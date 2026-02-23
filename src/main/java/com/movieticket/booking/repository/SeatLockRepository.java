package com.movieticket.booking.repository;

import com.movieticket.booking.domain.SeatLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional
public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatLock s WHERE s.showId = :showId AND s.seatNumber = :seat")
    Optional<SeatLock> lockSeat(@Param("showId") Long showId,
                                @Param("seat") String seat);

    Optional<SeatLock> findByShowIdAndSeatNumber(Long showId, String seatNumber);

    List<SeatLock> findByShowId(Long showId);

    @Modifying
    @Query("DELETE FROM SeatLock s WHERE s.showId = :showId AND s.seatNumber = :seat")
    void unlockSeat(@Param("showId") Long showId,
                    @Param("seat") String seat);
}