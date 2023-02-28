package me.sonam.jwt;

import me.sonam.jwt.json.HmacBody;
import me.sonam.security.util.HmacKeyJson;
import me.sonam.security.jwt.JwtBody;
import me.sonam.security.jwt.JwtCreator;
import me.sonam.security.jwt.PublicKeyJwtCreator;
import me.sonam.security.jwt.repo.HmacKeyRepository;
import me.sonam.security.jwt.repo.entity.HmacKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static me.sonam.security.jwt.PublicKeyJwtCreator.getJson;
import static me.sonam.security.util.Util.getHmacKeyFromJson;

@Service
public class JwtService implements Jwt {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    private JwtCreator jwtCreator;

    @Autowired
    private HmacKeyRepository hmacKeyRepository;

    @Autowired
    private HmacKeyJson hmacKeyJson;

    private Random random = new SecureRandom();

    public JwtService() {
    }


    @Override
    public Mono<String> create(Mono<JwtBody> jwtBodyMono) {
        LOG.info("create jwt token using the body");

        return jwtBodyMono.flatMap(jwtBody -> jwtCreator.create(jwtBody));

    }

    @Override
    public Mono<String> getPublicKey(UUID keyId) {
        LOG.info("get public key for keyId: {}", keyId);

        return jwtCreator.getPublicKey(keyId);
    }

    @Override
    public Mono<String> getKeyId(Mono<String> jwtMono) {
        LOG.info("get keyId from jwt");
        init();
        return jwtMono.flatMap(jwt -> jwtCreator.getKeyId(jwt));
    }

    @Override
    public Mono<String> generateHmac(final String algoirthm, Mono<String> monoData, final String key) {
        LOG.info("generate hmac");
        return monoData.flatMap(data ->
        Mono.just(PublicKeyJwtCreator.getHmac(algoirthm, data, key)));
    }

    @Override
    public Mono<String> generateHmac(Mono<HmacBody> hmacBodyMono) {
        LOG.info("generate hmac2 ");
        return hmacBodyMono.flatMap(hmacBody -> {
            LOG.info("hmacBody: {}", hmacBody);
                 return Mono.just(PublicKeyJwtCreator.getHmac(hmacBody.getAlgorithm(), hmacBody.getData(), hmacBody.getKey()));});
    }

    @Override
    public Mono<String> createHmacKey(String clientId) {
        LOG.info("create hmacKey entity");
        final String secretKey = generateSecureRandomPassword();

        return hmacKeyRepository.save(
                new HmacKey(true, clientId, secretKey, PublicKeyJwtCreator.Md5Algorithm.HmacSHA256.name(), true))
                .flatMap(hmacKey -> Mono.just(getJson(hmacKey)));
    }

    public Stream<Character> getRandomSpecialChars(int count) {
        Random random = new SecureRandom();
        IntStream specialChars = random.ints(count, 33, 45);
        return specialChars.mapToObj(data -> (char) data);
    }

    public String generateSecureRandomPassword() {
        Stream<Character> pwdStream = Stream.concat(getRandomNumbers(2),
                Stream.concat(getRandomSpecialChars(2),
                        Stream.concat(getRandomAlphabets(2, true), getRandomAlphabets(4, false))));
        List<Character> charList = pwdStream.collect(Collectors.toList());
        Collections.shuffle(charList);
        String password = charList.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return password;
    }

    public Stream<Character> getRandomNumbers(int count) {
        IntStream numbers = random.ints(count, 48, 57);
        return numbers.mapToObj(data -> (char) data);
    }

    public Stream<Character> getRandomAlphabets(int count, boolean upperCase) {
        IntStream characters = null;
        if (upperCase) {
            characters = random.ints(count, 65, 90);
        } else {
            characters = random.ints(count, 97, 122);
        }
        return characters.mapToObj(data -> (char) data);
    }

    //@PostConstruct
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
