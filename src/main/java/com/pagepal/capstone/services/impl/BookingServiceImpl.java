package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.booking.*;
import com.pagepal.capstone.dtos.event.EventDto;
import com.pagepal.capstone.dtos.meeting.MeetingDto;
import com.pagepal.capstone.dtos.notification.NotificationCreateDto;
import com.pagepal.capstone.dtos.pagination.PagingDto;
import com.pagepal.capstone.dtos.recording.MeetingRecordings;
import com.pagepal.capstone.dtos.recording.RecordingDto;
import com.pagepal.capstone.entities.postgre.*;
import com.pagepal.capstone.entities.postgre.Record;
import com.pagepal.capstone.enums.*;
import com.pagepal.capstone.mappers.BookingMapper;
import com.pagepal.capstone.mappers.SeminarMapper;
import com.pagepal.capstone.repositories.*;
import com.pagepal.capstone.services.*;
import com.pagepal.capstone.utils.DateUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Transactional
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final BookingStateRepository bookingStateRepository;
    private final WorkingTimeRepository workingTimeRepository;
    private final MeetingRepository meetingRepository;
    private final SettingRepository settingRepository;

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ReaderRepository readerRepository;
    private final WebhookService webhookService;
    private final NotificationService notificationService;
    private final ZoomService zoomService;
    private final DateUtils dateUtils;
    private final FirebaseMessagingService firebaseMessagingService;
    private final String pagePalLogoUrl = "https://firebasestorage.googleapis.com/v0/b/authen-6cf1b.appspot.com/o/private_image%2F1.png?alt=media&token=56384e72-69dc-4ab3-8ede-9401b6f2f121";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final String bookingCancel = "CANCEL";
    private final String bookingPending = "PENDING";
    private final String bookingComplete = "COMPLETE";
    private final String revenueString = "REVENUE_SHARE";
    private final String tokenPriceString = "TOKEN_PRICE";
    private final String dollarExchangeString = "DOLLAR_EXCHANGE_RATE";
    private final RecordRepository recordRepository;
    private final RecordFileRepository recordFileRepository;
    private final ReportRepository reportRepository;
    private final EventRepository eventRepository;


    @Secured("READER")
    @Override
    public ListBookingDto getListBookingByReader(UUID readerId, QueryDto queryDto) {

        if (queryDto.getPage() == null || queryDto.getPage() < 0)
            queryDto.setPage(0);
        if (queryDto.getPageSize() == null || queryDto.getPageSize() < 0)
            queryDto.setPageSize(10);

        Pageable pageable;
        if (queryDto.getSort() != null && queryDto.getSort().equals("desc")) {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").descending());
        } else {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").ascending());
        }


        Page<Booking> bookings;

        String state = queryDto.getBookingState().toUpperCase();

        if (queryDto.getBookingState() == null || queryDto.getBookingState().isEmpty())
            bookings = bookingRepository.findAllByReaderIdAndServiceIsNotNull(readerId, pageable);
        else {
            Date currentTime = dateUtils.getCurrentVietnamDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            Date modifiedTime = calendar.getTime();
            if ("PENDING".equals(state)) {
                bookings = bookingRepository.findAllByReaderIdAndBookingStatePendingAndServiceIsNotNull(readerId, modifiedTime, pageable);
            } else if ("PROCESSING".equals(state)) {
                bookings = bookingRepository.findByServiceStateProcessingAndReaderId(readerId, modifiedTime, pageable);
            } else {
                bookings = bookingRepository
                        .findAllByReaderIdAndBookingStateAndServiceIsNotNull(readerId,
                                queryDto.getBookingState().toUpperCase(),
                                pageable);
            }

        }

        ListBookingDto listBookingDto = new ListBookingDto();

        if (bookings == null) {
            listBookingDto.setList(Collections.emptyList());
            listBookingDto.setPagination(null);
        } else {
            PagingDto pagingDto = new PagingDto();
            pagingDto.setTotalOfPages(bookings.getTotalPages());
            pagingDto.setTotalOfElements(bookings.getTotalElements());
            pagingDto.setSort(bookings.getSort().toString());
            pagingDto.setCurrentPage(bookings.getNumber());
            pagingDto.setPageSize(bookings.getSize());

            listBookingDto.setList(bookings.map(this::toDtoIncludeRecording).toList());
            listBookingDto.setPagination(pagingDto);
        }
        return listBookingDto;
    }

    @Override
    public ListBookingDto getListEventBookingByReader(UUID readerId, QueryDto queryDto) {
        if (queryDto.getPage() == null || queryDto.getPage() < 0)
            queryDto.setPage(0);
        if (queryDto.getPageSize() == null || queryDto.getPageSize() < 0)
            queryDto.setPageSize(10);

        Pageable pageable;
        if (queryDto.getSort() != null && queryDto.getSort().equals("desc")) {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").descending());
        } else {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").ascending());
        }


        Page<Event> events;
        List<Booking> bookings = new ArrayList<>();

        String state = queryDto.getBookingState().toUpperCase();


        Date currentTime = dateUtils.getCurrentVietnamDate();
        events = switch (state) {
            case "PENDING" ->
                    eventRepository.findAllEventActiveByReaderId(readerId, EventStateEnum.ACTIVE, currentTime, pageable);
            case "PROCESSING" -> eventRepository.findAllEventProcessingByReaderId(readerId, currentTime, pageable);
            case "COMPLETE" -> eventRepository.findAllEventCompletedByReaderId(readerId, currentTime, pageable);
            default -> eventRepository.findAllEventCanceledByReaderId(readerId, currentTime, pageable);
        };

        ListBookingDto listBookingDto = new ListBookingDto();

        if (events == null || events.isEmpty()) {
            listBookingDto.setList(Collections.emptyList());
            listBookingDto.setPagination(null);
        } else {

            for (var event : events.getContent()) {
                var booking = bookingRepository.findFirstByEventOrderByCreateAtDesc(event);
                if (booking == null) {
                    booking = new Booking();
                    booking.setEvent(event);
                }
                bookings.add(booking);
            }

            PagingDto pagingDto = new PagingDto();
            pagingDto.setTotalOfPages(events.getTotalPages());
            pagingDto.setTotalOfElements(events.getTotalElements());
            pagingDto.setSort(events.getSort().toString());
            pagingDto.setCurrentPage(events.getNumber());
            pagingDto.setPageSize(events.getSize());

            listBookingDto.setList(bookings.stream().map(this::toDtoIncludeRecording).toList());
            listBookingDto.setPagination(pagingDto);
        }
        return listBookingDto;
    }

    @Secured({"CUSTOMER", "READER"})
    @Override
    public ListBookingDto getListBookingByCustomer(UUID cusId, QueryDto queryDto) {

        if (queryDto.getPage() == null || queryDto.getPage() < 0)
            queryDto.setPage(0);
        if (queryDto.getPageSize() == null || queryDto.getPageSize() < 0)
            queryDto.setPageSize(10);

        Pageable pageable;

        if (queryDto.getSort() != null && queryDto.getSort().equals("desc")) {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").descending());
        } else {
            pageable = PageRequest.of(queryDto.getPage(), queryDto.getPageSize(), Sort.by("startAt").ascending());
        }

        Page<Booking> bookings;

        Customer customer = customerRepository
                .findById(cusId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        String state = queryDto.getBookingState().toUpperCase();

        if (queryDto.getBookingState() == null || queryDto.getBookingState().isEmpty())
            bookings = bookingRepository.findByCustomer(customer, pageable);
        else {
            Date currentTime = dateUtils.getCurrentVietnamDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            Date modifiedTime = calendar.getTime();
            if ("PENDING".equals(state)) {
                bookings = bookingRepository.findByStatePendingAndCustomerId(cusId, modifiedTime, pageable);
            } else if ("PROCESSING".equals(state)) {
                bookings = bookingRepository.findByStateProcessingAndCustomerId(cusId, modifiedTime, pageable);
            } else {
                bookings = bookingRepository
                        .findAllByCustomerIdAndBookingState(cusId,
                                queryDto.getBookingState().toUpperCase(),
                                pageable);
            }
        }

        ListBookingDto listBookingDto = new ListBookingDto();
        if (bookings == null) {
            listBookingDto.setList(Collections.emptyList());
            listBookingDto.setPagination(null);
            return listBookingDto;
        } else {
            PagingDto pagingDto = new PagingDto();
            pagingDto.setTotalOfPages(bookings.getTotalPages());
            pagingDto.setTotalOfElements(bookings.getTotalElements());
            pagingDto.setSort(bookings.getSort().toString());
            pagingDto.setCurrentPage(bookings.getNumber());
            pagingDto.setPageSize(bookings.getSize());

            listBookingDto.setList(bookings.map(this::toDtoIncludeRecording).toList());
            listBookingDto.setPagination(pagingDto);
            return listBookingDto;
        }
    }

    @Secured({"CUSTOMER", "READER"})
    @Override
    public BookingDto createBooking(UUID cusId, BookingCreateDto bookingDto) {
        Customer customer = customerRepository
                .findById(cusId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        WorkingTime wt = workingTimeRepository
                .findById(bookingDto.getWorkingTimeId())
                .orElseThrow(() -> new EntityNotFoundException("Working time not found"));
        //        String startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(wt.getStartTime());
        var service = serviceRepository.findById(bookingDto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        if (customer.getAccount().getId() == service.getReader().getAccount().getId()) {
            throw new ValidationException("Cannot book your own service");
        }

        wt.getBookings().forEach(booking -> {
            if (booking.getState().getName().equals(bookingPending)) {
                throw new ValidationException("Working time is not available");
            }
        });

        Meeting meeting = zoomService.createMeeting(wt.getReader(), service.getBook().getTitle(),
                60, service.getServiceType().getName(), wt.getStartTime());

        if (meeting == null) {
            throw new RuntimeException("Failed to create meeting");
        }

        int tokenLeft = customer.getAccount().getWallet().getTokenAmount() - bookingDto.getTotalPrice();
        if (tokenLeft < 0) {
            throw new ValidationException("Not enough token");
        }
        Booking booking = new Booking();
        booking.setCreateAt(dateUtils.getCurrentVietnamDate());
        booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
        booking.setDescription(bookingDto.getDescription());
        booking.setPromotionCode(bookingDto.getPromotionCode());
        booking.setCustomer(customer);
        booking.setTotalPrice(bookingDto.getTotalPrice());
        booking.setMeeting(meeting);
        booking.setWorkingTime(wt);
        booking.setStartAt(wt.getStartTime());
        booking.setService(service);
        booking.setState(
                bookingStateRepository
                        .findByName("PENDING")
                        .orElseThrow(() -> new EntityNotFoundException("State not found"))
        );

        Booking res = bookingRepository.save(booking);
        if (res != null) {
            customer.getAccount().getWallet().setTokenAmount(tokenLeft);
            customerRepository.save(customer);

            Transaction transaction = new Transaction();
            transaction.setStatus(TransactionStatusEnum.SUCCESS);
            transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
            transaction.setTransactionType(TransactionTypeEnum.BOOKING_PAYMENT);
            transaction.setCurrency(CurrencyEnum.TOKEN);
            transaction.setBooking(res);
            transaction.setAmount(Double.valueOf(bookingDto.getTotalPrice()));
            transaction.setWallet(customer.getAccount().getWallet());
            transactionRepository.save(transaction);

            // Send webhook to discord
            Map<String, String> content = new HashMap<>();
            content.put("Content", "New booking from " + customer.getFullName());
            content.put("Customer-Id", customer.getId().toString());
            content.put("Booking-Id", res.getId().toString());
            content.put("Reader", service.getReader().getNickname());
            content.put("Reader-Id", service.getReader().getId().toString());
            content.put("Service", service.getServiceType().getName() + " - " + service.getBook().getTitle());
            content.put("Service-Id", service.getId().toString());
            webhookService.sendWebhookWithData(customer.getAccount(), content, Boolean.TRUE, Boolean.FALSE);

            String title = "New Booking";
            String readerContent = "Service" + " - " + service.getBook().getTitle() + " has booked " + "by " + customer.getFullName();
            String customerContent = "Service" + " - " + service.getBook().getTitle() + " has booked successfully. (- " + bookingDto.getTotalPrice() + " pals)";

            // Save notification to reader
            NotificationCreateDto readerNotification = new NotificationCreateDto();
            readerNotification.setAccountId(wt.getReader().getAccount().getId());
            readerNotification.setTitle(title);
            readerNotification.setContent(readerContent);
            readerNotification.setNotificationRole(NotificationRoleEnum.READER);
            notificationService.createNotification(readerNotification);

            // Save notification to customer
            NotificationCreateDto customerNotification = new NotificationCreateDto();
            customerNotification.setAccountId(customer.getAccount().getId());
            customerNotification.setTitle(title);
            customerNotification.setContent(customerContent);
            customerNotification.setNotificationRole(NotificationRoleEnum.CUSTOMER);
            notificationService.createNotification(customerNotification);

            // Send notification to web (reader/customer)
            String readerFcmWebToken = wt.getReader().getAccount().getFcmWebToken();
            String customerFcmWebToken = customer.getAccount().getFcmWebToken();

            if (customerFcmWebToken != null && !customerFcmWebToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        title,
                        customerContent,
                        Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                        customerFcmWebToken
                );

                if (readerFcmWebToken != null && !readerFcmWebToken.trim().isEmpty() && !readerFcmWebToken.equals(customerFcmWebToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmWebToken
                    );
                }
            } else {
                if (readerFcmWebToken != null && !readerFcmWebToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmWebToken
                    );
                }
            }

            // Send notification to mobile (reader/customer)
            String readerFcmMobileToken = wt.getReader().getAccount().getFcmMobileToken();
            String customerFcmMobileToken = customer.getAccount().getFcmMobileToken();

            if (customerFcmMobileToken != null && !customerFcmMobileToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        title,
                        customerContent,
                        Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                        customerFcmMobileToken
                );

                if (readerFcmMobileToken != null && !readerFcmMobileToken.trim().isEmpty() && !readerFcmMobileToken.equals(customerFcmMobileToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmMobileToken
                    );
                }
            } else {
                if (readerFcmMobileToken != null && !readerFcmMobileToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", res.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmMobileToken
                    );
                }
            }
        }
        return toDtoIncludeRecording(res);
    }

    @Override
    public BookingDto cancelBooking(UUID id, String reason) {
        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (!booking.getState().getName().equals(bookingPending)) {
            throw new ValidationException("Booking already canceled or completed!");
        }
        int durationInMinutes = getDurationInMinutes(dateUtils.getCurrentVietnamDate(), booking.getStartAt());

        if (durationInMinutes < 60) {
            throw new ValidationException("Cannot cancel booking less than 60 minutes before start time");
        }

        Customer customer = booking.getCustomer();
        int tokenLeft = customer.getAccount().getWallet().getTokenAmount() + booking.getTotalPrice();
        customer.getAccount().getWallet().setTokenAmount(tokenLeft);
        customer = customerRepository.save(customer);

        if (customer == null) {
            throw new ValidationException("Cannot refund token");
        }

        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatusEnum.SUCCESS);
        transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
        transaction.setTransactionType(TransactionTypeEnum.BOOKING_REFUND);
        transaction.setCurrency(CurrencyEnum.TOKEN);
        transaction.setBooking(booking);
        transaction.setAmount(Double.valueOf(booking.getTotalPrice()));
        transaction.setWallet(customer.getAccount().getWallet());
        var transact = transactionRepository.save(transaction);

        BookingState state = bookingStateRepository
                .findByName(bookingCancel)
                .orElseThrow(() -> new EntityNotFoundException("State not found"));
        booking.setState(state);
        booking.setCancelReason(reason);
        booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
        booking = bookingRepository.save(booking);

        if (transact != null && booking != null) {
            WorkingTime wt = booking.getWorkingTime();
            var service = booking.getService();

            // Send webhook to discord
            Map<String, String> content = new HashMap<>();
            content.put("Booking", booking.getId() + " has canceled");
            content.put("Content", "Cancel Booking by " + customer.getFullName());
            content.put("Customer-Id", customer.getId().toString());
            content.put("Reader", service.getReader().getNickname());
            content.put("Reader-Id", service.getReader().getId().toString());
            content.put("Service", service.getServiceType().getName() + " - " + service.getBook().getTitle());
            content.put("Service-Id", service.getId().toString());
            webhookService.sendWebhookWithData(customer.getAccount(), content, Boolean.TRUE, Boolean.TRUE);

            String title = "Booking Canceled";
            String readerContent = "Service" + service.getBook().getTitle() + " has canceled " + "by " + customer.getFullName();
            String customerContent = "Service" + service.getBook().getTitle() + " has canceled successfully " + "( + " + booking.getTotalPrice() + " pals)";

            // Send notification to reader
            NotificationCreateDto readerNotification = new NotificationCreateDto();
            readerNotification.setTitle(title);
            readerNotification.setAccountId(wt.getReader().getAccount().getId());
            readerNotification.setContent(readerContent);
            readerNotification.setNotificationRole(NotificationRoleEnum.READER);
            notificationService.createNotification(readerNotification);

            // Send notification refund token to customer
            NotificationCreateDto customerNotification = new NotificationCreateDto();
            customerNotification.setTitle(title);
            customerNotification.setAccountId(customer.getAccount().getId());
            customerNotification.setContent(customerContent);
            customerNotification.setNotificationRole(NotificationRoleEnum.CUSTOMER);
            notificationService.createNotification(customerNotification);

            // Send notification to web (reader/customer)
            String readerFcmWebToken = wt.getReader().getAccount().getFcmWebToken();
            String customerFcmWebToken = customer.getAccount().getFcmWebToken();


            if (customerFcmWebToken != null && !customerFcmWebToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        title,
                        customerContent,
                        Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                        customerFcmWebToken
                );

                if (readerFcmWebToken != null && !readerFcmWebToken.trim().isEmpty() && !readerFcmWebToken.equals(customerFcmWebToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmWebToken
                    );
                }
            } else {
                if (readerFcmWebToken != null && !readerFcmWebToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmWebToken
                    );
                }
            }

            // Send notification to mobile (reader/customer)
            String readerFcmMobileToken = wt.getReader().getAccount().getFcmMobileToken();
            String customerFcmMobileToken = customer.getAccount().getFcmMobileToken();

            if (customerFcmMobileToken != null && !customerFcmMobileToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        title,
                        customerContent,
                        Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                        customerFcmMobileToken
                );

                if (readerFcmMobileToken != null && !readerFcmMobileToken.trim().isEmpty() && !readerFcmMobileToken.equals(customerFcmMobileToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmMobileToken
                    );
                }
            } else {
                if (readerFcmMobileToken != null && !readerFcmMobileToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            readerContent,
                            readerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            readerFcmMobileToken
                    );
                }
            }
        }
        return toDtoIncludeRecording(booking);
    }

    @Override
    public BookingDto completeBooking(UUID id) {
        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (!booking.getState().getName().equals(bookingPending)) {
            throw new ValidationException("Booking already canceled or completed!");
        }

        RecordingDto recording = zoomService.getRecording(booking.getMeeting().getMeetingCode());

        if (recording == null) {
            throw new RuntimeException("Cannot complete booking, recording not found");
        }

        int durationInMinutes = getDurationInMinutes(recording.getRecording_files().get(0).getRecording_start(),
                recording.getRecording_files().get(0).getRecording_end());

        if (durationInMinutes < 40) {
            throw new ValidationException("Cannot complete booking, recording duration less than 40 minutes");
        }

        Setting revenue = settingRepository.findByKey(revenueString).orElse(null);
        Setting tokenPrice = settingRepository.findByKey(tokenPriceString).orElse(null);

        if (revenue == null || tokenPrice == null) {
            throw new EntityNotFoundException("Setting not found");
        }

        Float receiveCash = ((booking.getTotalPrice() * Float.parseFloat(tokenPrice.getValue()))
                * (100 - Float.parseFloat(revenue.getValue()))) / 100;
        Wallet wallet = booking.getService().getReader().getAccount().getWallet();
        wallet.setCash(wallet.getCash() + receiveCash);
        wallet = walletRepository.save(wallet);

        if (wallet != null) {

            Transaction transaction = new Transaction();
            transaction.setStatus(TransactionStatusEnum.SUCCESS);
            transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
            transaction.setTransactionType(TransactionTypeEnum.BOOKING_DONE_RECEIVE);
            transaction.setCurrency(CurrencyEnum.DOLLAR);
            transaction.setBooking(booking);
            transaction.setAmount(Double.valueOf(receiveCash));
            transaction.setWallet(booking.getService().getReader().getAccount().getWallet());
            transactionRepository.save(transaction);

            BookingState state = bookingStateRepository
                    .findByName(bookingComplete)
                    .orElseThrow(() -> new EntityNotFoundException("State not found"));
            booking.setState(state);
            booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
            booking = bookingRepository.save(booking);

            var service = booking.getService();
            service.setTotalOfBooking(service.getTotalOfBooking() + 1);
            serviceRepository.save(service);

            var reader = service.getReader();
            reader.setTotalOfBookings(reader.getTotalOfBookings() + 1);
            readerRepository.save(reader);

            var customer = booking.getCustomer();

            // Send webhook to discord
            Map<String, String> content = new HashMap<>();
            content.put("Booking", booking.getId() + " has completed");
            content.put("Customer-Id", customer.getId().toString());
            content.put("Reader", service.getReader().getNickname());
            content.put("Reader-Id", service.getReader().getId().toString());
            content.put("Service", service.getServiceType().getName() + " - " + service.getBook().getTitle());
            content.put("Service-Id", service.getId().toString());
            webhookService.sendWebhookWithData(customer.getAccount(), content, Boolean.TRUE, Boolean.FALSE);

            String title = "Booking Completed";
            String readerContent = "Service" + service.getBook().getTitle() + " has completed success " + "(+ " + receiveCash + " pals)";
            String customerContent = "Service" + service.getBook().getTitle() + " has completed by " + reader.getNickname() + ". Please review this service";

            NotificationCreateDto readerNotification = new NotificationCreateDto();
            readerNotification.setTitle(title);
            readerNotification.setAccountId(reader.getAccount().getId());
            readerNotification.setContent(readerContent);
            readerNotification.setNotificationRole(NotificationRoleEnum.READER);
            notificationService.createNotification(readerNotification);

            NotificationCreateDto customerNotification = new NotificationCreateDto();
            customerNotification.setTitle(title);
            customerNotification.setAccountId(customer.getAccount().getId());
            customerNotification.setContent(customerContent);
            customerNotification.setNotificationRole(NotificationRoleEnum.CUSTOMER);
            notificationService.createNotification(customerNotification);

            // Send notification to web (reader/customer)
            String readerFcmWebToken = reader.getAccount().getFcmWebToken();
            String customerFcmWebToken = customer.getAccount().getFcmWebToken();


            if (readerFcmWebToken != null && !readerFcmWebToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        readerContent,
                        readerContent,
                        Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                        readerFcmWebToken
                );
                if (customerFcmWebToken != null && !customerFcmWebToken.trim().isEmpty() && !readerFcmWebToken.equals(customerFcmWebToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            title,
                            customerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            customerFcmWebToken
                    );
                }
            } else {
                if (customerFcmWebToken != null && !customerFcmWebToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            title,
                            customerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            customerFcmWebToken
                    );
                }
            }

            // Send notification to mobile (reader/customer)
            String readerFcmMobileToken = reader.getAccount().getFcmMobileToken();
            String customerFcmMobileToken = customer.getAccount().getFcmMobileToken();

            if (readerFcmMobileToken != null && !readerFcmMobileToken.trim().isEmpty()) {
                firebaseMessagingService.sendNotificationToDevice(
                        pagePalLogoUrl,
                        readerContent,
                        readerContent,
                        Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                        readerFcmMobileToken
                );
                if (customerFcmMobileToken != null && !customerFcmMobileToken.trim().isEmpty() && !readerFcmMobileToken.equals(customerFcmMobileToken)) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            title,
                            customerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            customerFcmMobileToken
                    );
                }
            } else {
                if (customerFcmMobileToken != null && !customerFcmMobileToken.trim().isEmpty()) {
                    firebaseMessagingService.sendNotificationToDevice(
                            pagePalLogoUrl,
                            title,
                            customerContent,
                            Map.of("bookingId", booking.getId().toString(), "customerId", customer.getId().toString()),
                            customerFcmMobileToken
                    );
                }
            }
        }

        return toDtoIncludeRecording(booking);
    }

    @Override
    public EventDto completeEventBooking(UUID id) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (event.getState() == EventStateEnum.INACTIVE) {
            throw new ValidationException("Event is already deleted!");
        }

        BookingState state = bookingStateRepository
                .findByName(bookingPending)
                .orElseThrow(() -> new EntityNotFoundException("State not found"));
        List<Booking> bookings = bookingRepository.findByEventAndState(event, state);

        if (bookings == null || bookings.isEmpty()) {
            throw new ValidationException("Event has no booking!");
        }

        RecordingDto recording = zoomService.getRecording(bookings.get(0).getMeeting().getMeetingCode());

        if (recording == null) {
            throw new RuntimeException("Cannot complete booking, recording not found");
        }

        int durationInMinutes = getDurationInMinutes(recording.getRecording_files().get(0).getRecording_start(),
                recording.getRecording_files().get(0).getRecording_end());

        var requiredDuration = event.getSeminar().getDuration() * 0.8;

        if (durationInMinutes < requiredDuration) {
            throw new ValidationException("Cannot complete booking, recording duration less than 80% event duration");
        }

        Setting revenue = settingRepository.findByKey(revenueString).orElse(null);
        Setting tokenPrice = settingRepository.findByKey(tokenPriceString).orElse(null);

        if (revenue == null || tokenPrice == null) {
            throw new EntityNotFoundException("Setting not found");
        }

        int totalPrice = event.getPrice() * bookings.size();
        Float receiveCash = ((totalPrice * Float.parseFloat(tokenPrice.getValue()))
                * (100 - Float.parseFloat(revenue.getValue()))) / 100;
        Wallet wallet = event.getSeminar().getReader().getAccount().getWallet();
        wallet.setCash(wallet.getCash() + receiveCash);
        wallet = walletRepository.save(wallet);

        if (wallet != null) {
            Transaction transaction = new Transaction();
            transaction.setStatus(TransactionStatusEnum.SUCCESS);
            transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
            transaction.setTransactionType(TransactionTypeEnum.BOOKING_DONE_RECEIVE);
            transaction.setCurrency(CurrencyEnum.DOLLAR);
            transaction.setAmount(Double.valueOf(receiveCash));
            transaction.setWallet(event.getSeminar().getReader().getAccount().getWallet());
            transactionRepository.save(transaction);
            for (var booking : bookings) {
                state = bookingStateRepository
                        .findByName(bookingComplete)
                        .orElseThrow(() -> new EntityNotFoundException("State not found"));
                booking.setState(state);
                booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
                bookingRepository.save(booking);
            }
        }

        return SeminarMapper.INSTANCE.toEventDto(event);
    }

    private static int getDurationInMinutes(Date start, Date end) {
        long durationInMillis = end.getTime() - start.getTime();

        // Convert the duration to minutes
        long durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);

        return (int) (durationInSeconds / 60);
    }

    //@Secured("CUSTOMER")
    @Override
    public BookingDto reviewBooking(UUID id, ReviewBooking review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        if (!booking.getState().getName().equals(bookingComplete)) {
            throw new ValidationException("Booking not completed yet!");
        }
        booking.setReview(review.getReview());
        booking.setRating(review.getRating());
        booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
        booking = bookingRepository.save(booking);
        var service = booking.getService();
        Reader reader = null;
        if (service != null) {
            reader = service.getReader();
            int serviceRating = (service.getRating() * service.getTotalOfReview());
            service.setTotalOfReview(service.getTotalOfReview() + 1);
            int newServiceRating = Math.round((serviceRating + review.getRating()) / (service.getTotalOfReview()));
            service.setRating(newServiceRating);
            serviceRepository.save(service);
        } else {
            reader = booking.getEvent().getSeminar().getReader();
        }

        int rating = (reader.getRating() * reader.getTotalOfReviews());
        reader.setTotalOfReviews(reader.getTotalOfReviews() + 1);
        int newRating = Math.round((rating + review.getRating()) / (reader.getTotalOfReviews()));
        reader.setRating(newRating);
        readerRepository.save(reader);

        return booking != null ? toDtoIncludeRecording(booking) : null;
    }

    @Override
    public BookingDto getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        Date endTime;
        if (booking.getService() != null) {
            endTime = new Date(booking.getStartAt().getTime() + 60 * 60 * 1000);
        } else {
            Integer duration = booking.getEvent().getSeminar().getDuration();
            endTime = new Date(booking.getStartAt().getTime() + duration * 60 * 1000);
        }
        Date currentTime = dateUtils.getCurrentVietnamDate();

        long timeDifferenceMillis = currentTime.getTime() - booking.getStartAt().getTime();

        if (timeDifferenceMillis >= 0 && timeDifferenceMillis <= 7200000) {
            booking = updateBookingRecord(booking);
        }

        return toDtoIncludeRecording2(booking);
    }

    private BookingDto toDtoIncludeRecording2(Booking booking) {
        BookingDto bookingDto = BookingMapper.INSTANCE.toDto(booking);
        boolean isReported = reportRepository.findByReportedIdAndType(booking.getId(), ReportTypeEnum.BOOKING).isEmpty();
        bookingDto.setIsReported(!isReported);
        return bookingDto;
    }

    @Override
    public Integer updateRecordByBookingId(UUID id) {

        int count = 0;

        List<Booking> bookings = bookingRepository
                .findByStartAtBetween(
                        new Date(dateUtils.getCurrentVietnamDate().getTime() - 20 * 24 * 60 * 60 * 1000),
                        dateUtils.getCurrentVietnamDate());
        for (var booking : bookings) {
            boolean isUpdated = updateBookingRecord2(booking);
            if (isUpdated) count++;
        }

        return count;
    }

    private boolean updateBookingRecord2(Booking booking) {

        boolean isUpdated = false;

        Meeting meeting = booking.getMeeting();

        MeetingRecordings recording = zoomService.getListRecordingByMeetingId(booking.getMeeting().getMeetingCode());

        List<RecordingDto> recordingList = recording.getMeetings();

        if (recordingList == null || recordingList.isEmpty()) {
            return false;
        }

        List<Record> listRecord = new ArrayList<>();

        for (var recordDto : recordingList) {
            if (recordRepository.findByExternalId(recordDto.getUuid()).isPresent()) {
                continue;
            }

            Record record = new Record();
            record.setRecordingCount(recordDto.getRecording_count());
            record.setStatus(Status.ACTIVE);
            record.setDuration(recordDto.getDuration());
            record.setExternalId(recordDto.getUuid());
            record.setStartTime(recordDto.getStart_time());
            record.setMeeting(meeting);

            record = recordRepository.save(record);

            Record finalRecord = record;
            List<RecordFile> listRecordFile = recordDto.getRecording_files().stream().map(file -> {
                return RecordFile
                        .builder()
                        .downloadUrl(file.getDownload_url())
                        .playUrl(file.getPlay_url())
                        .fileExtention(file.getFile_extension())
                        .fileType(file.getFile_type())
                        .startAt(file.getRecording_start())
                        .endAt(file.getRecording_end())
                        .recordingType(file.getRecording_type())
                        .status(Status.ACTIVE)
                        .record(finalRecord)
                        .build();
            }).toList();

            listRecordFile = recordFileRepository.saveAll(listRecordFile);
            record.setRecordFiles(listRecordFile);
            listRecord.add(record);
            isUpdated = true;
        }

        return isUpdated;
    }

    @Override
    public Booking updateBookingRecord(Booking booking) {

        Meeting meeting = booking.getMeeting();

        MeetingRecordings recording = zoomService.getListRecordingByMeetingId(booking.getMeeting().getMeetingCode());

        List<RecordingDto> recordingList = recording.getMeetings();

        if (recordingList == null || recordingList.isEmpty()) {
            return booking;
        }

        List<Record> listRecord = new ArrayList<>();

        for (var recordDto : recordingList) {
            Record record = recordRepository.findByExternalId(recordDto.getUuid()).orElse(new Record());

            record.setRecordingCount(recordDto.getRecording_count());
            record.setStatus(Status.ACTIVE);
            record.setDuration(recordDto.getDuration());
            record.setExternalId(recordDto.getUuid());
            record.setStartTime(recordDto.getStart_time());
            record.setMeeting(meeting);

            record = recordRepository.save(record);

            List<RecordFile> recordFiles = record.getRecordFiles();
            if (recordFiles != null && !recordFiles.isEmpty()) {
                recordFileRepository.deleteAll(recordFiles);
            }
            Record finalRecord = record;
            List<RecordFile> listRecordFile = recordDto.getRecording_files().stream().map(file -> {
                return RecordFile
                        .builder()
                        .downloadUrl(file.getDownload_url())
                        .playUrl(file.getPlay_url())
                        .fileExtention(file.getFile_extension())
                        .fileType(file.getFile_type())
                        .startAt(file.getRecording_start())
                        .endAt(file.getRecording_end())
                        .recordingType(file.getRecording_type())
                        .status(Status.ACTIVE)
                        .record(finalRecord)
                        .build();
            }).toList();

            listRecordFile = recordFileRepository.saveAll(listRecordFile);
            record.setRecordFiles(listRecordFile);
            listRecord.add(record);
        }

        return bookingRepository.findById(booking.getId()).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }

    private static String generateRoomId(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    private BookingDto toDtoIncludeRecording(Booking booking) {


//        MeetingDto meeting = bookingDto.getMeeting();
//
//        MeetingRecordings recording = zoomService.getListRecordingByMeetingId(booking.getMeeting().getMeetingCode());
//        meeting.setRecordings(recording);
//        bookingDto.setMeeting(meeting);
        return BookingMapper.INSTANCE.toDto(booking);
    }
}
