package ke.co.suncha.simba.aqua.scheme.zone.meterReader;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import ke.co.suncha.simba.admin.models.User;
import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 8/30/17.
 */
@Service
public class MeterReaderService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MeterReaderRepository meterReaderRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ZoneService zoneService;

    @Autowired
    UserService userService;

    public MeterReader addMeterReaderToZone(Long userId, Long zoneId) {
        if (!isMeterReaderInZone(userId, zoneId)) {
            MeterReader meterReader = new MeterReader();
            meterReader.setZone(zoneService.getById(zoneId));
            meterReader.setUser(userService.getById(userId));
            meterReader = meterReaderRepository.save(meterReader);
            return meterReader;
        }
        return null;
    }

    public void removeMeterReaderFromZone(Long userId, Long zoneId) {
        if (isMeterReaderInZone(userId, zoneId)) {
            MeterReader meterReader = getMeterReader(userId, zoneId);
            if (meterReader != null) {
                meterReaderRepository.delete(meterReader);
            }
        }
    }

    public List<User> getZoneMeterReaders(Long zoneId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReader.meterReader.zone.zoneId.eq(zoneId));
        JPAQuery query = new JPAQuery(entityManager);
        List<MeterReader> meterReaderList = query.from(QMeterReader.meterReader).where(builder).list(QMeterReader.meterReader);
        List<User> userList = new ArrayList<>();

        if (meterReaderList != null) {
            for (MeterReader meterReader : meterReaderList) {
                userList.add(meterReader.getUser());
            }
        }
        return userList;
    }

    public List<User> getMeterReaders() {
        List<User> userList = new ArrayList<>();
        List<Long> zoneIdList = zoneService.getZoneIdList();
        if (zoneIdList != null) {
            for (Long zoneId : zoneIdList) {
                List<User> users = getZoneMeterReaders(zoneId);
                if (users != null) {
                    if (!users.isEmpty()) {
                        for (User user : users) {
                            if (!userList.contains(user)) {
                                userList.add(user);
                            }
                        }
                    }
                }
            }
        }
        return userList;
    }

    public List<User> getMeterReadersNotInZone(Long zoneId) {
        List<User> meterReaderListInZone = getZoneMeterReaders(zoneId);
        List<Long> userIdsInZone = new ArrayList<>();
        if (meterReaderListInZone != null) {
            for (User user : meterReaderListInZone) {
                userIdsInZone.add(user.getUserId());
            }
        }

        List<User> userList = userService.getUserList();
        List<User> meterReadersNotInZone = new ArrayList<>();
        if (userList != null) {
            for (User user : userList) {
                if (!userIdsInZone.contains(user.getUserId())) {
                    meterReadersNotInZone.add(user);
                }
            }
        }
        return meterReadersNotInZone;
    }

    public List<Zone> getZonesWithMeterReader(Long userId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReader.meterReader.user.userId.eq(userId));
        JPAQuery query = new JPAQuery(entityManager);
        List<MeterReader> meterReaderList = query.from(QMeterReader.meterReader).where(builder).list(QMeterReader.meterReader);
        List<Zone> zoneList = new ArrayList<>();

        if (meterReaderList != null) {
            for (MeterReader meterReader : meterReaderList) {
                zoneList.add(meterReader.getZone());
            }
        }
        return zoneList;
    }

    public MeterReader getMeterReader(Long userId, Long zoneId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReader.meterReader.user.userId.eq(userId));
        builder.and(QMeterReader.meterReader.zone.zoneId.eq(zoneId));
        JPAQuery query = new JPAQuery(entityManager);
        return query.from(QMeterReader.meterReader).where(builder).singleResult(QMeterReader.meterReader);
    }

    public Boolean isMeterReaderInZone(Long userId, Long zoneId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMeterReader.meterReader.user.userId.eq(userId));
        builder.and(QMeterReader.meterReader.zone.zoneId.eq(zoneId));
        JPAQuery query = new JPAQuery(entityManager);
        Long count = query.from(QMeterReader.meterReader).where(builder).count();
        if (count == null) count = 0l;
        if (count > 0)
            return Boolean.TRUE;

        return Boolean.FALSE;
    }
}