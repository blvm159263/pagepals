//package com.pagepal.capstone.services.impl;
//
//import com.pagepal.capstone.entities.postgre.*;
//import com.pagepal.capstone.enums.LoginTypeEnum;
//import com.pagepal.capstone.enums.Status;
//import com.pagepal.capstone.repositories.BookRepository;
//import com.pagepal.capstone.repositories.ReaderRepository;
//import com.pagepal.capstone.repositories.ServiceRepository;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.Date;
//import java.util.UUID;
//
//@ContextConfiguration(classes = {ServiceProvideServiceImpl.class})
//@ExtendWith(SpringExtension.class)
//public class ServiceProvideServiceTest {
//    @MockBean
//    private ServiceRepository serviceRepository;
//    @MockBean
//    private ReaderRepository readerRepository;
//    @MockBean
//    private BookRepository bookRepository;
//    @Autowired
//    private ServiceProvideServiceImpl serviceProvideServiceImpl;
//
//    UUID id = UUID.randomUUID();
//
//    AccountState accountState1 = new AccountState(UUID.randomUUID(), "ACTIVE", Status.ACTIVE, null);
//    AccountState accountState2 = new AccountState(UUID.randomUUID(), "INACTIVE", Status.ACTIVE, null);
//
//    //Role
//    Role role1 = new Role(UUID.randomUUID(), "READER", Status.ACTIVE, null);
//    Role role2 = new Role(UUID.randomUUID(), "CUSTOMER", Status.ACTIVE, null);
//
//    //Account
//    Account account1 = new Account(UUID.randomUUID(), "username1", "password1", "email1", "fullName1", "0123456789", LoginTypeEnum.NORMAL,
//            new Date(), new Date(), new Date(), accountState1, null, null, null, null, role1, null, null);
//    Account account2 = new Account(UUID.randomUUID(), "username2", "password2", "email2", "fullName1", "0123456789", LoginTypeEnum.NORMAL,
//            new Date(), new Date(), new Date(), accountState2, null, null, null, null, role2, null, null);
//
//
//    Reader reader = new Reader(id, "nickname", 3, "Fiction",
//            "US", "US", "", "description", 0, 0,
//            "", "avt", new Date(), new Date(), null, Status.ACTIVE, null, null, account1,
//            null, null,
//            null, null, null, null
//    );
//
//    Category category = new Category(UUID.randomUUID(), "name", "description",
//            Status.ACTIVE, null
//    );
//
//    Book book = new Book(UUID.randomUUID(), "name", "long title", "author", "publisher",
//            "20L", 200, "overview", "imageUrl", "edition",
//            Status.ACTIVE, null, null, null, null
//    );
//
//    ServiceType serviceType = new ServiceType(UUID.randomUUID(), "name", "description",
//            Status.ACTIVE, null
//    );
//
//    Service service = new Service(UUID.randomUUID(), 255,
//            new Date(), "description", 10.0,
//            1, 1, 1, false,
//            Status.ACTIVE,
//            reader, book, serviceType
//    );
//
////    @Test
////    void testGetAllServicesByReaderId() {s
////        reader.setServices(List.of(service));
////
////        var queryDto = new QueryDto();
////        queryDto.setPage(0);
////        queryDto.setPageSize(10);
////        queryDto.setSearch("name");
////        queryDto.setSort("asc");
////
////        Pageable pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("createdAt").ascending());
////
////        when(serviceRepository.findAllByReaderAndBookTitleContainsIgnoreCase(reader, queryDto.getSearch(), pageable))
////                .thenReturn(new PageImpl<>(List.of(service)));
////
////        var actual = serviceProvideServiceImpl.getAllServicesByReaderId(id, queryDto);
////
////        assert actual.size() > 0;
////    }
//}
