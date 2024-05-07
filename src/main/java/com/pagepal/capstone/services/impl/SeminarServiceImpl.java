package com.pagepal.capstone.services.impl;

import com.pagepal.capstone.dtos.pagination.PagingDto;
import com.pagepal.capstone.dtos.recording.RecordingDto;
import com.pagepal.capstone.dtos.seminar.*;
import com.pagepal.capstone.entities.postgre.*;
import com.pagepal.capstone.enums.*;
import com.pagepal.capstone.mappers.BookingMapper;
import com.pagepal.capstone.mappers.SeminarMapper;
import com.pagepal.capstone.repositories.*;
import com.pagepal.capstone.services.BookService;
import com.pagepal.capstone.services.SeminarService;
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

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class SeminarServiceImpl implements SeminarService {
    private final BookingStateRepository bookingStateRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final SeminarRepository seminarRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final BookingRepository bookingRepository;
    private final MeetingRepository meetingRepository;
    private final ZoomServiceImpl zoomService;
    private final DateUtils dateUtils;
    private final BookService bookService;
    private final SettingRepository settingRepository;
    private final WalletRepository walletRepository;

    private final String revenueString = "REVENUE_SHARE";
    private final String tokenPriceString = "TOKEN_PRICE";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_SEMINARS_PER_DAY = 5;
    private static final int MAX_SEMINARS_PER_WEEK = 12;
    private static final int MAX_SEMINARS_PER_MONTH = 25;

    // Check if creating a seminar exceeds the limit
    public boolean canCreateSeminar(UUID readerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = startOfDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime startOfMonth = startOfDay.with(TemporalAdjusters.firstDayOfMonth());

        // convert to date
        Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date startOfDayDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        Date startOfWeekDate = Date.from(startOfWeek.atZone(ZoneId.systemDefault()).toInstant());
        Date startOfMonthDate = Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        long seminarsToday = seminarRepository.countByReaderIdAndCreatedAtBetween(readerId, startOfDayDate, nowDate);
        long seminarsThisWeek = seminarRepository.countByReaderIdAndCreatedAtBetween(readerId, startOfWeekDate, nowDate);
        long seminarsThisMonth = seminarRepository.countByReaderIdAndCreatedAtBetween(readerId, startOfMonthDate, nowDate);

        return seminarsToday <= MAX_SEMINARS_PER_DAY &&
                seminarsThisWeek <= MAX_SEMINARS_PER_WEEK &&
                seminarsThisMonth <= MAX_SEMINARS_PER_MONTH;
    }

    @Secured({"READER"})
    @Override
    public SeminarDto createSeminarRequest(SeminarCreateDto seminarCreateDto) {

        Reader reader = readerRepository.findById(seminarCreateDto.getReaderId()).orElseThrow(() -> new EntityNotFoundException("Reader not found"));

        if (Boolean.FALSE.equals(canCreateSeminar(reader.getId()))) {
            throw new ValidationException("Reader exceeds the limit of creating seminar requests");
        }

        Book book = bookRepository.findByExternalId(seminarCreateDto.getBook().getId()).orElse(null);

        if (book == null) {
            book = bookService.createNewBook(seminarCreateDto.getBook());
        }


        Seminar seminar = new Seminar();
        seminar.setTitle(seminarCreateDto.getTitle());
        seminar.setDescription(seminarCreateDto.getDescription());
        seminar.setImageUrl(seminarCreateDto.getImageUrl());
        seminar.setRejectReason(null);
        seminar.setDuration(seminarCreateDto.getDuration());
        seminar.setState(SeminarStatus.PENDING);
        seminar.setCreatedAt(dateUtils.getCurrentVietnamDate());
        seminar.setUpdatedAt(dateUtils.getCurrentVietnamDate());
        seminar.setReader(reader);
        seminar.setBook(book);
        seminar.setEvents(null);
        seminar.setStatus(Status.INACTIVE);

        return SeminarMapper.INSTANCE.toDto(seminarRepository.save(seminar));

    }

    @Secured({"READER"})
    @Override
    public SeminarDto updateSeminarRequest(SeminarUpdateDto seminarUpdateDto) {

        Seminar seminar = seminarRepository.findById(seminarUpdateDto.getId()).orElseThrow(() -> new EntityNotFoundException("Seminar not found"));

        if (seminar.getState() != SeminarStatus.PENDING) {
            throw new ValidationException("Seminar is not available for update");
        }

        seminar.setId(seminarUpdateDto.getId());
        seminar.setTitle(seminarUpdateDto.getTitle());
        seminar.setDescription(seminarUpdateDto.getDescription());
        seminar.setImageUrl(seminarUpdateDto.getImageUrl());
        seminar.setDuration(seminarUpdateDto.getDuration());
        seminar.setUpdatedAt(dateUtils.getCurrentVietnamDate());

        return SeminarMapper.INSTANCE.toDto(seminarRepository.save(seminar));
    }

    @Secured({"READER"})
    @Override
    public SeminarDto deleteSeminarRequest(UUID id) {
        Seminar seminar = seminarRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Seminar not found"));
        if (seminar.getState() != SeminarStatus.PENDING) {
            throw new ValidationException("Seminar is not available for delete");
        } else if (seminar.getStatus() == Status.INACTIVE) {
            throw new ValidationException("Seminar is already deleted");
        }
        seminar.setStatus(Status.INACTIVE);
        return SeminarMapper.INSTANCE.toDto(seminarRepository.save(seminar));
    }

    @Override
    public ListSeminarDto getAllSeminarRequests(Integer page, Integer pageSize, String sort, SeminarStatus state) {
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (sort == null || sort.trim().isEmpty() || sort.trim().isBlank()) {
            sort = "asc";
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").ascending());
        if (sort.equals("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        }

        Page<Seminar> list = seminarRepository.findByStateAndStatus(state, Status.ACTIVE, pageable);

        ListSeminarDto listSeminarDto = new ListSeminarDto();

        PagingDto pagingDto = new PagingDto();
        pagingDto.setTotalOfPages(list.getTotalPages());
        pagingDto.setTotalOfElements(list.getTotalElements());
        pagingDto.setSort(list.getSort().toString());
        pagingDto.setCurrentPage(list.getNumber());
        pagingDto.setPageSize(list.getSize());

        // set value
        listSeminarDto.setList(list.map(SeminarMapper.INSTANCE::toDto).toList());
        listSeminarDto.setPagination(pagingDto);

        return listSeminarDto;
    }

    @Override
    public SeminarDto getSeminarRequest(UUID id) {
        var seminar = seminarRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Seminar not found"));
        if (seminar.getState() != SeminarStatus.PENDING) {
            throw new ValidationException("Seminar is not available for view");
        } else if (seminar.getStatus() == Status.INACTIVE) {
            throw new ValidationException("Seminar is already deleted");
        }
        return SeminarMapper.INSTANCE.toDto(seminar);
    }


//
//	@Override
//	public SeminarDto getSeminar(UUID id) {
//		var data = seminarRepository.findById(id).orElseThrow();
//		return toSeminarDto(data);
//	}
//
//	@Override
//	public ListSeminarDto getSeminarList(Integer page, Integer pageSize, String sort, String state) {
//		Pageable pageable = createPageable(page, pageSize, sort);
//		Page<Seminar> data = seminarRepository.findAllByStatus(SeminarStatus.valueOf(state), pageable);
//		return mapSeminarsToDto(data);
//	}
//
//	@Override
//	public ListSeminarDto getSeminarListByReaderId(UUID readerId, Integer page, Integer pageSize, String sort,
//			String state, Boolean isCustomer) {
//		Pageable pageable = createPageable(page, pageSize, sort);
//		Page<Seminar> data;
//
//		if(isCustomer){
//			data = seminarRepository.findAllByReaderIdAndStartTimeAfter(readerId, dateUtils.getCurrentVietnamDate() ,pageable);
//		}else{
//			if (!state.isEmpty() || !"".equals(state)) {
//				data = seminarRepository.findAllByReaderIdAndStatus(readerId, SeminarStatus.valueOf(state), pageable);
//			} else {
//				data = seminarRepository.findAllByReaderId(readerId, pageable);
//			}
//		}
//
//		return mapSeminarsToDto(data);
//	}
//
//	@Override
//	public ListSeminarDto getSeminarListByCustomerId(UUID customerId, Integer page, Integer pageSize, String sort) {
//		Pageable pageable = createPageable(page, pageSize, sort);
//		Page<Seminar> data = seminarRepository.findAllByCustomerId(customerId, pageable);
//		return mapSeminarsToDto(data);
//	}
//
//	@Override
//	public ListSeminarDto getSeminarListNotJoinByCustomerId(UUID customerId, Integer page, Integer pageSize,
//			String sort, String state) {
//		Pageable pageable = createPageable(page, pageSize, sort);
//		Page<Seminar> data = seminarRepository.findAllByCustomerIdNotJoin(customerId, SeminarStatus.valueOf(state),
//				pageable);
//		return mapSeminarsToDto(data);
//	}
//
//	@Secured({ "READER" })
//	@Override
//	public SeminarDto deleteSeminar(UUID id) {
//		var seminar = seminarRepository.findById(id).orElse(null);
//		if(seminar == null) {
//			throw new ValidationException("Seminar not found");
//		}
//
//		var bookings = seminar.getBookings();
//		if(bookings != null) {
//			var listBookingWithStatePending = bookings.stream()
//					.filter(booking -> booking.getState().getName().equals("PENDING")).toList();
//			if(listBookingWithStatePending != null && !listBookingWithStatePending.isEmpty()) {
//				throw new ValidationException("Seminar has booking with state pending, Cannot delete");
//			}
//
//			seminar.setStatus(SeminarStatus.INACTIVE);
//			return toSeminarDto(seminarRepository.save(seminar));
//		}
//
//		return null;
//	}
//
//	@Override
//	public SeminarBookingDto bookSeminar(UUID seminarId, UUID customerId) {
//		var customer = customerRepository.findById(customerId).orElse(null);
//		var seminar = seminarRepository.findById(seminarId).orElse(null);
//
//		if(customer.getAccount().getId().equals(seminar.getReader().getAccount().getId())) {
//			throw new ValidationException("Cannot book seminar with reader account");
//		}
//
//		if(customer == null || seminar == null) {
//			throw new ValidationException("Customer or Seminar not found");
//		}
//
//		if(seminar.getActiveSlot() <= 0) {
//			throw new ValidationException("Seminar is full");
//		}
//
//		Date currentTime = dateUtils.getCurrentVietnamDate();
//		Date seminarTime = seminar.getStartTime();
//
//		Date seminarTimeBefore24h = new Date(seminarTime.getTime() - 24 * 60 * 60 * 1000);
//		if(currentTime.after(seminarTimeBefore24h)) {
//			throw new ValidationException("Cannot book seminar within 24 hours before seminar start time");
//		}
//
//		List<Booking> bookings = seminar.getBookings();
//
//		if(bookings.size() >= 0) {
//			for(Booking booking : bookings) {
//				if(booking.getCustomer().getId().equals(customerId) && "PENDING".equals(booking.getState().getName())) {
//					throw new ValidationException("Customer already booked this seminar");
//				}
//			}
//		}
//
//		var account = customer.getAccount();
//		var wallet = account.getWallet().getTokenAmount();
//		int tokenLeft = wallet - seminar.getPrice();
//		if(tokenLeft < 0) {
//			throw new ValidationException("Not enough money");
//		}
//
//		Booking booking = new Booking();
//		booking.setTotalPrice(seminar.getPrice());
//		booking.setPromotionCode("");
//		booking.setDescription(seminar.getDescription());
//		booking.setRating(0);
//		booking.setCreateAt(dateUtils.getCurrentVietnamDate());
//		booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
//		booking.setStartAt(seminar.getStartTime());
//		booking.setCustomer(customer);
//		booking.setMeeting(seminar.getMeeting());
//		booking.setSeminar(seminar);
//		booking.setState(
//				bookingStateRepository
//						.findByName("PENDING")
//						.orElseThrow(() -> new EntityNotFoundException("State not found"))
//		);
//		booking = bookingRepository.save(booking);
//
//		if(booking != null) {
//			seminar.setActiveSlot(seminar.getActiveSlot() - 1);
//			seminarRepository.save(seminar);
//
//			customer.getAccount().getWallet().setTokenAmount(tokenLeft);
//			customerRepository.save(customer);
//
//			Transaction transaction = new Transaction();
//			transaction.setStatus(TransactionStatusEnum.SUCCESS);
//			transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
//			transaction.setTransactionType(TransactionTypeEnum.BOOKING_SEMINAR_DONE);
//			transaction.setCurrency(CurrencyEnum.TOKEN);
//			transaction.setBooking(booking);
//			transaction.setAmount(Double.valueOf(seminar.getPrice()));
//			transaction.setWallet(customer.getAccount().getWallet());
//			transactionRepository.save(transaction);
//		}
//
//		SeminarBookingDto seminarBookingDto = new SeminarBookingDto();
//		seminarBookingDto.setBooking(booking);
//		seminarBookingDto.setSeminar(seminar);
//
//		return seminarBookingDto;
//	}
//
//	@Secured({ "READER" })
//	@Override
//	public SeminarDto completeSeminar(UUID seminarId) {
//		Seminar seminar = seminarRepository.findById(seminarId).orElse(null);
//		if(seminar != null) {
//
//			if(SeminarStatus.INACTIVE.equals(seminar.getStatus())) {
//				throw new ValidationException("Seminar is completed or deleted!");
//			}
//
//			Integer durationSeminar = seminar.getDuration();
//			RecordingDto recording = zoomService.getRecording(seminar.getMeeting().getMeetingCode());
//
//			if (recording == null) {
//				throw new RuntimeException("Cannot complete booking, recording not found");
//			}
//
//			int durationInMinutes = getDurationInMinutes(recording.getRecording_files().get(0).getRecording_start(),
//					recording.getRecording_files().get(0).getRecording_end());
//
//			if (durationInMinutes < (int) (durationSeminar * 0.8)) {
//				throw new ValidationException("Cannot complete booking, recording duration less than 40 minutes");
//			}
//
//			List<Booking> bookings = seminar.getBookings();
//			Integer totalOfBooking = 0;
//
//			if(bookings != null && !bookings.isEmpty()) {
//				BookingState bookingStateComplete = bookingStateRepository
//						.findByName("COMPLETE")
//						.orElseThrow(() -> new ValidationException("State not found"));
//				for(Booking booking : bookings) {
//					if("PENDING".equals(booking.getState().getName())) {
//						booking.setState(bookingStateComplete);
//						booking.setUpdateAt(dateUtils.getCurrentVietnamDate());
//						bookingRepository.save(booking);
//
//						totalOfBooking++;
//					}
//				}
//
//				Integer totalOfMoneyBeforeValueShare = seminar.getPrice() * totalOfBooking;
//				Setting revenue = settingRepository.findByKey(revenueString).orElse(null);
//				Setting tokenPrice = settingRepository.findByKey(tokenPriceString).orElse(null);
//
//				if (revenue == null || tokenPrice == null) {
//					throw new EntityNotFoundException("Setting not found");
//				}
//
//				Float receiveCash = ((totalOfMoneyBeforeValueShare * Float.parseFloat(tokenPrice.getValue()))
//						* (100 - Float.parseFloat(revenue.getValue()))) / 100;
//
//				Account account = seminar.getReader().getAccount();
//				if(account != null) {
//					Wallet wallet = account.getWallet();
//					if(wallet != null) {
//						wallet.setCash(wallet.getCash() + receiveCash);
//						wallet = walletRepository.save(wallet);
//						if(wallet != null) {
//							Transaction transaction = new Transaction();
//							transaction.setStatus(TransactionStatusEnum.SUCCESS);
//							transaction.setCreateAt(dateUtils.getCurrentVietnamDate());
//							transaction.setTransactionType(TransactionTypeEnum.BOOKING_DONE_RECEIVE);
//							transaction.setCurrency(CurrencyEnum.DOLLAR);
//							transaction.setAmount(Double.valueOf(receiveCash));
//							transaction.setWallet(wallet);
//							transactionRepository.save(transaction);
//						}
//					}
//				}
//
//				seminar.setStatus(SeminarStatus.INACTIVE);
//				seminar.setUpdatedAt(dateUtils.getCurrentVietnamDate());
//				seminarRepository.save(seminar);
//			}
//			return toSeminarDto(seminar);
//		}
//		return null;
//	}
//
//	private ListSeminarDto mapSeminarsToDto(Page<Seminar> seminars) {
//		var listSeminarDto = new ListSeminarDto();
//
//		if(seminars == null) {
//			listSeminarDto.setList(Collections.emptyList());
//			listSeminarDto.setPagination(null);
//			return listSeminarDto;
//		} else {
//			var pagingDto = new PagingDto();
//			pagingDto.setTotalOfPages(seminars.getTotalPages());
//			pagingDto.setTotalOfElements(seminars.getTotalElements());
//			pagingDto.setSort(seminars.getSort().toString());
//			pagingDto.setCurrentPage(seminars.getNumber());
//			pagingDto.setPageSize(seminars.getSize());
//
//			listSeminarDto.setList(seminars.map(this::toSeminarDto).toList());
//			listSeminarDto.setPagination(pagingDto);
//
//			return listSeminarDto;
//		}
//	}
//
//	private Pageable createPageable(Integer page, Integer pageSize, String sort) {
//		if(page == null || page < 0) {
//			page = 0;
//		}
//
//		if(pageSize == null || pageSize < 0) {
//			pageSize = 10;
//		}
//
//		if(sort != null && sort.equals("desc")) {
//			return PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
//		} else {
//			return PageRequest.of(page, pageSize, Sort.by("createdAt").ascending());
//		}
//	}
//
//	private Reader checkWorkingTime(UUID readerId, SeminarCreateDto seminarCreateDto, UUID seminarId,
//			SeminarUpdateDto seminarUpdateDto) throws Exception {
//		Reader reader = readerRepository.findById(readerId).orElseThrow(EntityNotFoundException::new);
//		List<WorkingTime> workingTimes = reader.getWorkingTimes();
//		List<Seminar> seminars = seminarRepository.findByReaderIdAndStatus(readerId, SeminarStatus.ACTIVE);
//
//		LocalDateTime startTime;
//		if(seminarCreateDto != null) {
//			startTime = LocalDateTime.parse(seminarCreateDto.getStartTime(),
//					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//		} else {
//			startTime = LocalDateTime.parse(seminarUpdateDto.getStartTime(),
//					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//		}
//		int duration = (seminarCreateDto != null) ? seminarCreateDto.getDuration() : seminarUpdateDto.getDuration();
//
//		LocalDateTime endTime = startTime.plusMinutes(duration);
//
//		workingTimes.forEach(workingTime -> {
//			LocalDateTime workingStartTime = LocalDateTime.ofInstant(workingTime.getStartTime().toInstant(),
//					ZoneId.of("Asia/Ho_Chi_Minh"));
//			LocalDateTime workingEndTime = LocalDateTime.ofInstant(workingTime.getEndTime().toInstant(),
//					ZoneId.of("Asia/Ho_Chi_Minh"));
//
//			if((startTime.isAfter(workingStartTime) || startTime.isEqual(workingStartTime)) && startTime.isBefore(
//					workingEndTime)) {
//				throw new RuntimeException("Seminar start time is during working time");
//			}
//
//			if((endTime.isAfter(workingStartTime) || endTime.isEqual(workingStartTime)) && endTime.isBefore(
//					workingEndTime)) {
//				throw new RuntimeException("Seminar end time is during working time");
//			}
//		});
//
//		seminars.forEach(seminar -> {
//			if(seminarId != null && seminar.getId().equals(seminarId)) {
//				return;
//			}
//			LocalDateTime seminarStartTime = LocalDateTime.ofInstant(seminar.getStartTime().toInstant(),
//					ZoneId.of("Asia/Ho_Chi_Minh"));
//			LocalDateTime seminarEndTime = seminarStartTime.plusMinutes(seminar.getDuration());
//
//			if((startTime.isAfter(seminarStartTime) || startTime.isEqual(seminarStartTime)) && startTime.isBefore(
//					seminarEndTime)) {
//				throw new RuntimeException("Seminar start time conflicts with an existing seminar");
//			}
//
//			if((endTime.isAfter(seminarStartTime) || endTime.isEqual(seminarStartTime)) && endTime.isBefore(
//					seminarEndTime)) {
//				throw new RuntimeException("Seminar end time conflicts with an existing seminar");
//			}
//		});
//		return reader;
//	}

//	private SeminarDto toSeminarDto(Seminar seminar) {
//		return SeminarDto.builder()
//				.id(seminar.getId())
//				.title(seminar.getTitle())
//				.limitCustomer(seminar.getLimitCustomer())
//				.activeSlot(seminar.getActiveSlot())
//				.description(seminar.getDescription())
//				.imageUrl(seminar.getImageUrl())
//				.duration(seminar.getDuration())
//				.price(seminar.getPrice())
//				.startTime(dateFormat.format(seminar.getStartTime()))
//				.status(seminar.getStatus().name())
//				.createdAt(dateFormat.format(seminar.getCreatedAt()))
//				.updatedAt(dateFormat.format(seminar.getUpdatedAt()))
//				.reader(seminar.getReader())
//				.book(seminar.getBook())
//				.meeting(seminar.getMeeting())
//				.bookings(
//						seminar.getBookings() == null
//								? Collections.emptyList()
//								: seminar.getBookings().stream().map(BookingMapper.INSTANCE::toDto).toList()
//				)
//				.build();
//	}

    private static int getDurationInMinutes(Date start, Date end) {
        long durationInMillis = end.getTime() - start.getTime();

        // Convert the duration to minutes
        long durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);

        return (int) (durationInSeconds / 60);
    }
}
