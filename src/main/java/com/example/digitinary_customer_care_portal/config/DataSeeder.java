package com.example.digitinary_customer_care_portal.config;

import com.example.digitinary_customer_care_portal.entity.*;
import com.example.digitinary_customer_care_portal.enums.OrderStatus;
import com.example.digitinary_customer_care_portal.enums.Role;
import com.example.digitinary_customer_care_portal.enums.TicketPriority;
import com.example.digitinary_customer_care_portal.enums.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final PasswordEncoder encoder;
    private final Random random = new Random(42);

    public DataSeeder(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    private static final String[] FIRST = {"Omar", "Sara", "Layla", "Khaled", "Noor", "Yousef", "Dana", "Hadi", "Rania", "Tariq"};
    private static final String[] LAST = {"Haddad", "Nasser", "Khoury", "Saleh", "Mansour", "Awad", "Barakat", "Qasem"};
    private static final String[] ITEMS = {"Wireless Mouse", "Keyboard", "USB-C Cable", "Monitor", "Headphones",
            "Webcam", "Laptop Stand", "Power Bank", "Router", "SSD 1TB"};
    private static final String[] SUBJECTS = {"Order not delivered", "Wrong item received", "Refund request",
            "Cannot log in", "Damaged package", "Billing question", "Change delivery address", "Product not working"};

    @Override
    @Transactional
    public void run(String... args) {

        Long adminCount = em.createQuery(
                "select count(u) from User u where u.username = 'admin'", Long.class).getSingleResult();
        if (adminCount == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            em.persist(admin);
        }

        Long existing = em.createQuery("select count(c) from Customer c", Long.class).getSingleResult();
        if (existing > 0) return;

        List<Item> items = new ArrayList<>();
        for (String name : ITEMS) {
            Item item = new Item();
            item.setName(name);
            item.setPrice(Math.round((5 + random.nextDouble() * 295) * 100) / 100.0);
            em.persist(item);
            items.add(item);
        }

        List<Customer> customers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Customer c = new Customer();
            String first = FIRST[random.nextInt(FIRST.length)];
            String last = LAST[random.nextInt(LAST.length)];
            c.setName(first + " " + last);
            c.setEmail(first.toLowerCase() + "." + last.toLowerCase() + i + "@example.com");
            c.setPhone("+9627" + (70000000 + random.nextInt(29999999)));
            c.setCreatedAt(daysAgo(60 + random.nextInt(300)));
            em.persist(c);
            customers.add(c);

            User u = new User();
            u.setUsername("customer" + i);
            u.setPassword(encoder.encode("password123"));
            u.setRole(Role.CUSTOMER);
            u.setCustomer(c);
            em.persist(u);
        }

        OrderStatus[] orderStatuses = OrderStatus.values();
        for (Customer c : customers) {
            int orderCount = 1 + random.nextInt(4);
            for (int i = 0; i < orderCount; i++) {
                Order order = new Order();
                order.setCustomer(c);
                order.setStatus(orderStatuses[random.nextInt(orderStatuses.length)]);
                order.setCreatedAt(daysAgo(random.nextInt(50)));

                Set<OrderItem> orderItems = new HashSet<>();
                Set<Item> used = new HashSet<>();
                double total = 0;
                int lines = 1 + random.nextInt(3);
                for (int j = 0; j < lines; j++) {
                    Item item = items.get(random.nextInt(items.size()));
                    if (!used.add(item)) continue;
                    int qty = 1 + random.nextInt(3);
                    orderItems.add(new OrderItem(order, item, qty));
                    total += item.getPrice() * qty;
                }
                order.setOrderItems(orderItems);
                order.setTotal(Math.round(total * 100) / 100.0);
                em.persist(order);
            }
        }

        TicketStatus[] ticketStatuses = TicketStatus.values();
        TicketPriority[] priorities = TicketPriority.values();
        for (Customer c : customers) {
            int ticketCount = random.nextInt(4);
            for (int i = 0; i < ticketCount; i++) {
                Ticket t = new Ticket();
                t.setCustomer(c);
                t.setSubject(SUBJECTS[random.nextInt(SUBJECTS.length)]);
                t.setStatus(ticketStatuses[random.nextInt(ticketStatuses.length)]);
                t.setPriority(priorities[random.nextInt(priorities.length)]);
                t.setCreatedAt(daysAgo(random.nextInt(40)));
                em.persist(t);
            }
        }

        System.out.println("Mock data seeded: admin/admin123 and customer1..customer10 / password123");
    }

    private Date daysAgo(int days) {
        return new Date(System.currentTimeMillis() - days * 24L * 60 * 60 * 1000);
    }
}