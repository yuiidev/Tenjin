package net.demozo.tenjin.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.demozo.tenjin.Tenjin;
import net.demozo.tenjin.test.models.BlogPost;
import net.demozo.tenjin.test.models.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TenjinTest {
    private static DataSource source;
    private static Logger logger = LoggerFactory.getLogger(TenjinTest.class);


    @BeforeAll
    public static void beforeAll() {
        Properties properties = new Properties();
        properties.setProperty("dataSourceClassName", "org.mariadb.jdbc.MariaDbDataSource");
        properties.setProperty("dataSource.user", "root");
        properties.setProperty("dataSource.password", "secret");
        properties.setProperty("dataSource.databaseName", "tenjin_test");

        source = new HikariDataSource(new HikariConfig(properties));
        Tenjin.init(source);
        Tenjin.setDebugEnabled(true);
    }

    @AfterAll
    public static void afterAll() {
        Tenjin.shutdown();
    }

    @Test
    public void singleObjectRetrieval() {
        var user = Tenjin.get(User.class, UUID.fromString("4350c451-a5b0-48b0-bd14-acdddffed260"));

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());
    }

    @Test
    public void singleObjectRetrievalAndSave() {
        var user = Tenjin.get(User.class, UUID.fromString("4350c451-a5b0-48b0-bd14-acdddffed260"));

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());

        user.save();
    }

    @Test
    public void allObjectsRetrieval() {
        var users = Tenjin.getAll(User.class);

        assertNotNull(users);
        assertEquals(4, users.size());

        for (var user : users) {
            assertNotNull(user);
            assertNotNull(user.getId());
            assertNotNull(user.getUsername());
            assertNotNull(user.getEmail());
        }
    }

    @Test
    public void belongsToOne() {
        BlogPost post = Tenjin.get(BlogPost.class, UUID.fromString("b78c95cd-4eba-46b8-92f0-cdc2080e608a"));

        assertNotNull(post);

        logger.info("Getting author.");

        assertNotNull(post.getAuthor());
    }

    @Test
    public void hasMany() {
        logger.info("Getting user.");
        User user = Tenjin.get(User.class, UUID.fromString("4350c451-a5b0-48b0-bd14-acdddffed260"));

        assertNotNull(user);

        logger.info("Getting posts.");

        var posts = user.getPosts();
        assertNotNull(posts);
        assertEquals(2, posts.size());
    }

    @Test
    public void multiLevel() {
        logger.info("Getting user.");
        User user = Tenjin.get(User.class, UUID.fromString("4350c451-a5b0-48b0-bd14-acdddffed260"));

        assertNotNull(user);

        logger.info("Getting posts.");

        var posts = user.getPosts();
        assertNotNull(posts);
        assertEquals(2, posts.size());

        logger.info("Getting authors.");

        for(BlogPost post : user.getPosts()) {
            assertNotNull(post.getAuthor());
        }
    }
}
