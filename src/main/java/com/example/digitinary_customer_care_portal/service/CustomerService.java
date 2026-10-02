package com.example.digitinary_customer_care_portal.service;

import com.example.digitinary_customer_care_portal.dto.customer.CreateCustomerRequest;
import com.example.digitinary_customer_care_portal.dto.customer.CustomerDto;
import com.example.digitinary_customer_care_portal.dto.order.OrderDto;
import com.example.digitinary_customer_care_portal.dto.ticket.TicketDto;
import com.example.digitinary_customer_care_portal.entity.Customer;
import com.example.digitinary_customer_care_portal.entity.User;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import com.example.digitinary_customer_care_portal.enums.Role;
import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import com.example.digitinary_customer_care_portal.exception.CustomerDoesNotExist;
import com.example.digitinary_customer_care_portal.exception.DuplicateEmail;
import com.example.digitinary_customer_care_portal.mapping.CustomerToDto;
import com.example.digitinary_customer_care_portal.mapping.OrderToDto;
import com.example.digitinary_customer_care_portal.mapping.TicketToDto;
import com.example.digitinary_customer_care_portal.repository.CustomerRepository;
import com.example.digitinary_customer_care_portal.repository.OrderRepository;
import com.example.digitinary_customer_care_portal.repository.TicketRepository;
import com.example.digitinary_customer_care_portal.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository,
                           TicketRepository ticketRepository,
                           UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest req, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) {
            return create(req);
        }

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.getCustomer() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a customer profile");
        }

        CustomerDto created = create(req);
        user.setCustomer(customerRepository.getReferenceById(created.id()));
        userRepository.save(user);
        return created;
    }

    public CustomerDto create(CreateCustomerRequest createCustomerRequest) {
        String email = createCustomerRequest.email();
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateEmail("Customer with email " + email + " already exists");
        }
        Customer customer = new Customer();
        customer.setName(createCustomerRequest.name());
        customer.setEmail(email);
        customer.setPhone(createCustomerRequest.phone());
        if (createCustomerRequest.createdAt() != null) {
            customer.setCreatedAt(createCustomerRequest.createdAt());
        }
        customerRepository.save(customer);
        return CustomerToDto.toDto(customer);
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerDoesNotExist("Customer with id " + id + " not found"));
        customerRepository.delete(customer);
    }

    public CustomerDto get(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerDoesNotExist("Customer with id " + id + " not found"));
        return CustomerToDto.toDto(customer);
    }

    public List<CustomerDto> getAll() {
        return customerRepository.findAll().stream()
                .map(CustomerToDto::toDto)
                .toList();
    }

    @Transactional
    public CustomerDto replace(Long id, CreateCustomerRequest createCustomerRequest) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerDoesNotExist("Customer with id " + id + " not found"));
        String email = createCustomerRequest.email();
        if (!customer.getEmail().equals(email) && customerRepository.existsByEmail(email)) {
            throw new DuplicateEmail("Customer with email " + email + " already exists");
        }
        customer.setName(createCustomerRequest.name());
        customer.setEmail(email);
        customer.setPhone(createCustomerRequest.phone());
        if (createCustomerRequest.createdAt() != null) {
            customer.setCreatedAt(createCustomerRequest.createdAt());
        }
        customerRepository.save(customer);
        return CustomerToDto.toDto(customer);
    }

    public List<OrderDto> getOrders(Long customerId, OrderStatus status, String sort, int page, int size) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerDoesNotExist("Customer with id " + customerId + " not found");
        }
        return orderRepository.search(customerId, status, buildPageable(sort, page, size))
                .getContent().stream()
                .map(OrderToDto::toDto)
                .toList();
    }

    public List<TicketDto> getTickets(Long customerId, TicketStatus status, TicketPriority priority,
                                      String sort, int page, int size) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerDoesNotExist("Customer with id " + customerId + " not found");
        }
        return ticketRepository.search(customerId, status, priority, buildPageable(sort, page, size))
                .getContent().stream()
                .map(TicketToDto::toDto)
                .toList();
    }

    private Pageable buildPageable(String sort, int page, int size) {
        Sort.Direction direction = "oldest".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(direction, "createdAt"));
    }

    @Transactional
    public void linkUser(Long customerId, String username) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerDoesNotExist("Customer with id " + customerId + " not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only users with CUSTOMER role can be linked");
        }

        if (user.getCustomer() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already linked to a customer");
        }

        if (userRepository.existsByCustomer_Id(customerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer is already linked to a user");
        }

        user.setCustomer(customer);
        userRepository.save(user);
    }
}