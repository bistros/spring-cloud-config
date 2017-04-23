package org.springframework.cloud.config.server.environment;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.support.GitCredentialsProviderFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("spring.cloud.config.server.git")
public class CombineJGitEnvironmentRepository extends MultipleJGitEnvironmentRepository {
    private List<SubGitRepository> extra = new ArrayList<>();

    public CombineJGitEnvironmentRepository(ConfigurableEnvironment environment) {
        super(environment);
    }

    @PostConstruct
    public void init() throws Exception {
        GitCredentialsProviderFactory credentialFactory = new GitCredentialsProviderFactory();
        super.setGitCredentialsProvider(credentialFactory.createFor(getUri(), getUsername(), getPassword(), getPassphrase()));
        super.afterPropertiesSet();
        for (JGitEnvironmentRepository repo : this.extra) {
            repo.setEnvironment(getEnvironment());
            if (getTimeout() != 0 && repo.getTimeout() == 0) {
                repo.setTimeout(getTimeout());
            }
            String user = repo.getUsername();
            String pass = repo.getPassword();
            String passphrase = repo.getPassphrase();
            if (user == null) {
                user = getUsername();
                pass = getPassword();
            }
            if (passphrase == null) {
                passphrase = getPassphrase();
            }
            repo.setGitCredentialsProvider(credentialFactory.createFor(repo.getUri(), user, pass, passphrase));
            repo.afterPropertiesSet();
        }
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        JGitEnvironmentRepository repo = getRepository(this, this.getUri());
        Environment environment = repo.findOne(application, profile, label);
        for (SubGitRepository subRepo : extra) {
            Environment subEnv = subRepo.findOne(application, profile, label);
            environment.addAll(subEnv.getPropertySources());
        }
        return environment;
    }


    public List<SubGitRepository> getExtra() {
        return extra;
    }

    public void setExtra(List<SubGitRepository> extra) {
        this.extra = extra;
    }

    public static class SubGitRepository extends JGitEnvironmentRepository {
        public SubGitRepository() {
            super(null);
        }

        public SubGitRepository(String uri) {
            this();
            setUri(uri);
        }
    }
}
