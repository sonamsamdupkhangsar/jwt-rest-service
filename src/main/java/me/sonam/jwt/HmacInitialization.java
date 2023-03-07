package me.sonam.jwt;

import me.sonam.security.jwt.repo.HmacKeyRepository;
import me.sonam.security.jwt.repo.entity.HmacKey;
import me.sonam.security.util.HmacKeyJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static me.sonam.security.util.Util.getHmacKeyFromJson;

@Profile({"live", "local"})
@Service
public class HmacInitialization {
    private static final Logger LOG = LoggerFactory.getLogger(HmacInitialization.class);

    @Autowired
    private HmacKeyJson hmacKeyJson;
    @Autowired
    private HmacKeyRepository hmacKeyRepository;

    @PostConstruct
    private void init() {
        LOG.info("initialize hmacKeys.length: {}, \n strings; {}", hmacKeyJson.getHmacKeys().size(), hmacKeyJson.getHmacKeys());

        hmacKeyJson.getHmacKeys().forEach(app -> {
            LOG.info("jsonString: {}", app.getApp());
            HmacKey hmacKey = getHmacKeyFromJson(app.getApp());
            if (hmacKey != null) {
                hmacKeyRepository.existsById(hmacKey.getId())
                        .filter(aBoolean ->  {
                            if (!aBoolean) {
                                hmacKeyRepository.save(hmacKey).subscribe(hmacKey1 -> LOG.info("Saved hmacKey:{}", hmacKey));
                            }
                            else {
                                LOG.error("hmacKey exists by clientId already");
                            }
                            return true;
                        }).subscribe(aBoolean -> LOG.info("hmac key initiatlization done"));
            }
        });
    }
}
