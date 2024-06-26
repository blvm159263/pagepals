//package com.pagepal.capstone.repositories;
//
//import com.pagepal.capstone.entities.postgre.*;
//import com.pagepal.capstone.enums.LoginTypeEnum;
//import com.pagepal.capstone.enums.Status;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ContextConfiguration;
//
//import java.util.Date;
//import java.util.UUID;
//
//@ContextConfiguration(classes = {
//        ServiceRepository.class,
//        ReaderRepository.class,
//})
//@EnableAutoConfiguration
//@EntityScan(basePackages = {"com.pagepal.capstone.entities.postgre"})
//@DataJpaTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
//public class ServiceRepositoryTest {
//
//    @Autowired
//    private ServiceRepository serviceRepository;
//
//    AccountState accountState1 = new AccountState(UUID.randomUUID(), "ACTIVE", Status.ACTIVE, null);
//    AccountState accountState2 = new AccountState(UUID.randomUUID(), "INACTIVE", Status.ACTIVE, null);
//
//    //Role
//    Role role1 = new Role(UUID.randomUUID(), "READER", Status.ACTIVE, null);
//    Role role2 = new Role(UUID.randomUUID(), "CUSTOMER", Status.ACTIVE, null);
//
//    //Account
//    Account account1 = new Account(UUID.randomUUID(), "username1", "password1", "email1","fullName1","0123456789", LoginTypeEnum.NORMAL,
//            new Date(), new Date(), new Date(), accountState1,null,null, null, null, role1, null, null);
//    Account account2 = new Account(UUID.randomUUID(), "username2", "password2", "email2","fullName1","0123456789", LoginTypeEnum.NORMAL,
//            new Date(), new Date(), new Date(), accountState2,null,null, null, null, role2, null, null);
//
//    Reader reader = new Reader(UUID.randomUUID(), "name1", 4, "genre1", "Vietnamese"
//            , "accent1", "link1", "des1", null,
//            null, "vid1","avt",
//            new Date(), new Date(), new Date(), Status.ACTIVE, null, null,null, null,
//            null, null, null, null,null);
//
//    Category category = new Category(UUID.randomUUID(), "name", "description",
//            Status.ACTIVE, null
//    );
//
//    Book book = new Book(UUID.randomUUID(), "name", "long title", "author", "publisher",
//            "20L", 200, "overview", "imageUrl", "edition",
//             Status.ACTIVE,null, null, null, null
//    );
//
//    @Test
//    void testCanFindServiceDescription() {
//        String description = "description";
//
//        Service service = new Service();
//        service.setPrice(300);
//        service.setCreatedAt(new Date());
//        service.setDescription(description);
//        service.setDuration(1.0);
//        service.setTotalOfReview(1);
//        service.setTotalOfBooking(1);
//        service.setRating(4);
//        service.setStatus(Status.ACTIVE);
//        service.setReader(reader);
//
//        var serviceEntity = serviceRepository.save(service);
//
//        Assertions.assertTrue(serviceRepository.findById(serviceEntity.getId()).isPresent());
//        Assertions.assertEquals(description, serviceRepository.findById(serviceEntity.getId()).get().getDescription());
//    }
//}
