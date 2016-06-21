package ke.co.suncha.simba.aqua.postbank;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import ke.co.suncha.simba.admin.api.AbstractRestHandler;
import ke.co.suncha.simba.admin.request.RestPageRequest;
import ke.co.suncha.simba.admin.request.RestRequestObject;
import ke.co.suncha.simba.admin.request.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by maitha.manyala on 6/21/16.
 */
@RestController
@RequestMapping(value = "/api/v1/postBankFiles")
@Api(value = "Post Bank", description = "Post bank API")
public class PostBankFileController extends AbstractRestHandler {
    @Autowired
    PostBankFileService postBankFileService;

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a post bank file.", notes = "")
    public RestResponse create(@RequestParam("token") String token, @RequestParam("file") MultipartFile file) {
        return this.postBankFileService.create(token, file);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    public RestResponse getAll(@RequestBody RestRequestObject<RestPageRequest> requestObject) {
        return postBankFileService.findAll(requestObject);
    }

    @RequestMapping(value = "byFile/{id}", method = RequestMethod.POST, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse getByFile(@PathVariable("id") Long fileId, @RequestBody RestRequestObject<PostBankFile> requestObject) {
        return postBankFileService.findTransactionsByFile(requestObject, fileId);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = {"application/json"}, produces = {"application/json"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse post(@RequestBody RestRequestObject<PostBankFile> requestObject) {
        return postBankFileService.post(requestObject);
    }

    @RequestMapping(value = "/one", method = RequestMethod.POST, consumes = {"application/json", "application/xml"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public RestResponse findOne(@RequestBody RestRequestObject<PostBankFile> requestObject) {
        return postBankFileService.findOne(requestObject);
    }
}
