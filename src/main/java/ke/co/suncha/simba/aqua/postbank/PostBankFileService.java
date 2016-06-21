package ke.co.suncha.simba.aqua.postbank;

import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by maitha.manyala on 6/21/16.
 */
public interface PostBankFileService {
    RestResponse create(String token, MultipartFile file);

    RestResponse post(RestRequestObject<PostBankFile> requestObject);

    RestResponse findAll(RestRequestObject<RestPageRequest> requestObject);

    RestResponse findTransactionsByFile(RestRequestObject<PostBankFile> requestObject, Long fileId);

    RestResponse findOne(RestRequestObject<PostBankFile> requestObject);
}
