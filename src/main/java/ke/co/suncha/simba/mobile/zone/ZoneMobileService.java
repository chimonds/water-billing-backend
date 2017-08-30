package ke.co.suncha.simba.mobile.zone;

import ke.co.suncha.simba.admin.service.UserService;
import ke.co.suncha.simba.aqua.scheme.zone.Zone;
import ke.co.suncha.simba.aqua.scheme.zone.ZoneService;
import ke.co.suncha.simba.aqua.scheme.zone.meterReader.MeterReaderService;
import ke.co.suncha.simba.mobile.MobileUser;
import ke.co.suncha.simba.mobile.request.RequestResponse;
import ke.co.suncha.simba.mobile.user.MobileUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maitha.manyala on 8/15/17.
 */
@Service
public class ZoneMobileService {

    @Autowired
    MobileUserAuthService mobileUserAuthService;

    @Autowired
    ZoneService zoneService;

    @Autowired
    MeterReaderService meterReaderService;

    @Autowired
    UserService userService;

    public RequestResponse<List<MZone>> getAll(MobileUser mobileUser) {

        RequestResponse<List<MZone>> response = new RequestResponse<>();
        List<MZone> mZoneList = new ArrayList<>();

        if (!mobileUserAuthService.canLogin(mobileUser)) {
            response.setError(Boolean.TRUE);
            response.setMessage("Access denied to this resource");
        } else {

            List<Zone> zoneList = zoneService.getAll();
            if (zoneList != null) {
                for (Zone zone : zoneList) {
                    MZone mZone = new MZone();
                    mZone.setZoneId(zone.getZoneId());
                    mZone.setName(zone.getName());
                    if (meterReaderService.isMeterReaderInZone(userService.getByEmailAddress(mobileUser.getEmail()).getUserId(), zone.getZoneId())) {
                        mZone.setTakeReadings(1);
                    }
                    mZoneList.add(mZone);
                }
            }
        }
        response.setError(Boolean.FALSE);
        response.setMessage("Data fetched successfully");
        response.setObject(mZoneList);
        return response;
    }
}