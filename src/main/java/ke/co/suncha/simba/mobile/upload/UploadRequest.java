package ke.co.suncha.simba.mobile.upload;

import ke.co.suncha.simba.mobile.MobileUser;

/**
 * Created by maitha.manyala on 9/3/17.
 */
public class UploadRequest {
    private MeterReadingRequest readingRequest;
    private MobileUser user;
    private String imagePayload;

    public MeterReadingRequest getReadingRequest() {
        return readingRequest;
    }

    public void setReadingRequest(MeterReadingRequest readingRequest) {
        this.readingRequest = readingRequest;
    }

    public MobileUser getUser() {
        return user;
    }

    public void setUser(MobileUser user) {
        this.user = user;
    }

    public String getImagePayload() {
        return imagePayload;
    }

    public void setImagePayload(String imagePayload) {
        this.imagePayload = imagePayload;
    }
}
