package ke.co.suncha.simba.aqua.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by maitha on 10/24/16.
 */
@Service
public class SystemOptionServiceImpl implements SystemOptionService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemOptionRepository systemOptionRepository;

    @Override
    public String getValue(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        SystemOption option = systemOptionRepository.findByName(name);
        if (option == null) {
            option = new SystemOption();
            option.setName(name);
            option = systemOptionRepository.save(option);
        }
        return option.getValue();
    }

    @Override
    public Boolean isStrictModeEnabled() {
        Boolean enabled = Boolean.FALSE;
        try {
            enabled = Boolean.parseBoolean(getValue("STRICT_MODE_ENABLED"));
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return enabled;
    }
}
