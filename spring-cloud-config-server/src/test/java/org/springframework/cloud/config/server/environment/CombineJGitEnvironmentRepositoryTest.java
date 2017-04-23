package org.springframework.cloud.config.server.environment;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.test.ConfigServerTestUtils;
import org.springframework.core.env.StandardEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.config.server.environment.CombineJGitEnvironmentRepository.*;

public class CombineJGitEnvironmentRepositoryTest {
    private StandardEnvironment environment = new StandardEnvironment();
    private CombineJGitEnvironmentRepository repository = new CombineJGitEnvironmentRepository(this.environment);

    @Before
    public void init() throws Exception {
        String defaultUri = ConfigServerTestUtils.prepareLocalRepo("test1-config-repo");
        this.repository.setUri(defaultUri);
        this.repository.setExtra(createSubRepository());
    }

    private List<SubGitRepository> createSubRepository() throws Exception {
        String repo2uri = ConfigServerTestUtils.prepareLocalRepo("test1-config-repo");
        SubGitRepository extra1 = new SubGitRepository(repo2uri);
        extra1.setEnvironment(this.environment);
        List<SubGitRepository> list = new ArrayList<>();
        list.add(extra1);
        return list;
    }

    @Test
    public void combinePropertySource() {
        Environment environment = this.repository.findOne("bar", "staging", "master");
        List<PropertySource> sourceList = environment.getPropertySources();
        assertThat(sourceList).size()
                              .isEqualTo(2);
    }
}