package com.example.crud.service;

import com.example.crud.dto.ChapterDto;
import com.example.crud.model.User;   // Если есть у вас, иначе сами создайте
import com.example.crud.model.Title; // Аналогично
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Аналог вашего FDATABASE.py
 */
@Service
@RequiredArgsConstructor
public class FDATABASEService {
    private final JdbcTemplate jdbcTemplate;

    // ===========================
    // Вспомогательные методы
    // ===========================

    /**
     * Аналог add_exp(user_id, amount)
     */
    public void addExp(Long userId, int amount) {
        try {
            String sql = "UPDATE users SET exp = exp + ? WHERE id = ?";
            jdbcTemplate.update(sql, amount, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Title> searchByName(String partial) {
        String sql = "SELECT * FROM titles WHERE LOWER(name_on_russian) LIKE ?";
        String param = "%" + partial.toLowerCase() + "%";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Title t = new Title();
            // заполнить поля
            return t;
        }, param);
    }

    /**
     * Аналог get_level(experience)
     * Возвращает (уровень, остаток exp, порог)
     */
    public int[] getLevel(int experience) {
        try {
            int experienceNeeded = 10;
            int level = 2;
            if(experience < experienceNeeded) {
                // уровень 1, exp = experience, порог = 10
                return new int[]{1, experience, experienceNeeded};
            } else {
                int ostatok = experience;
                while(ostatok >= experienceNeeded) {
                    ostatok = ostatok - experienceNeeded;
                    experienceNeeded += Math.round(experienceNeeded * 0.15f);
                    level++;
                }
                // Возвращаем {level-1, остаток, experienceNeeded}
                return new int[]{level - 1, ostatok, experienceNeeded};
            }
        } catch(Exception e) {
            e.printStackTrace();
            return new int[]{1, experience, 10};
        }
    }

    /**
     * Проверка корректности входных строк (^[a-zA-Z0-9_]+$)
     */
    public boolean isValidInput(String input) {
        return Pattern.matches("^[a-zA-Z0-9_]+$", input);
    }

    /**
     * Условный "numerize" — аналог numerize.numerize(int(...))
     * Простейшая реализация: 1000 -> 1K, 1_000_000 -> 1M и т.д.
     */
    public String numerize(int number) {
        if (number < 1000) return String.valueOf(number);
        if (number < 1_000_000) return (number / 1000) + "K";
        if (number < 1_000_000_000) return (number / 1_000_000) + "M";
        return (number / 1_000_000_000) + "B";
    }

    // ===========================
    // Методы по аналогии с Python
    // ===========================

    /**
     * add_user(username, password, email)
     */
    public boolean addUser(String username, String password, String email) {
        try {
            // Проверяем, что не занято
            String checkSql = "SELECT COUNT(*) FROM users WHERE email=? OR username=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, email, username);
            if (count > 0) {
                return false;
            }
            // Добавляем
            String insertSql = "INSERT INTO users(username,password,email) VALUES(?,?,?)";
            jdbcTemplate.update(insertSql, username, password, email);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * getUser(user_id)
     */
    public Map<String,Object> getUser(Long userId) {
        try {
            String sql = "SELECT * FROM users WHERE id=? LIMIT 1";
            return jdbcTemplate.queryForMap(sql, userId);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * getUserByEmailOrUsername(emailOrUsername)
     * Возвращает Map (если не используете собственную модель)
     */
    public Map<String,Object> getUserByEmailOrUsername(String emailOrUsername) {
        try {
            String sql = "SELECT * FROM users WHERE email=? OR username=? LIMIT 1";
            return jdbcTemplate.queryForMap(sql, emailOrUsername, emailOrUsername);
        } catch(EmptyResultDataAccessException e) {
            return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * add_profile_pic(user_id, img)
     */
    public void addProfilePic(Long userId, String imgPath) {
        try {
            String sql = "UPDATE users SET profile_pic=? WHERE id=?";
            jdbcTemplate.update(sql, imgPath, userId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add_description(user_id, description)
     */
    public void addDescription(Long userId, String description) {
        try {
            String sql = "UPDATE users SET description=? WHERE id=?";
            jdbcTemplate.update(sql, description, userId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get_another_user(username)
     * Возвращает (id, user, profile_pic, description, status, exp)
     */
    public Object[] getAnotherUser(String username) {
        try {
            String sql = "SELECT id, username, profile_pic, description, status, exp FROM users WHERE username=? LIMIT 1";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return new Object[]{
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("profile_pic"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getInt("exp")
                };
            }, username);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * add_title(...) — Добавляет запись в таблицу TITLES
     */
    public void addTitle(String rus, String eng, String description, int year, String coverUrl, String genres, String type) {
        try {
            String sql = "INSERT INTO titles(name_on_russian, name_on_english, description, year, cover, genres, type) " +
                    "VALUES(?,?,?,?,?,?,?)";
            jdbcTemplate.update(sql, rus, eng, description, year, coverUrl, genres, type);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getTitle(title_name_on_english) — Возвращает поля тайтла + rating и т.д.
     *  Возвращает массив/объект, где [title_id, name_on_english, name_on_russian, description, year, cover, genres, amount_of_views, glava_count, rating]
     */
    public Object[] getTitle(String titleEnglish) {
        try {
            // Для упрощения получим все поля, rating — возьмём среднее
            String sql = """
                SELECT DISTINCT t.title_id,
                                t.name_on_english,
                                t.name_on_russian,
                                t.description,
                                t.year,
                                t.cover,
                                t.genres,
                                t.amount_of_views,
                                (SELECT count(*) FROM glava WHERE title_id=t.title_id) +
                                (SELECT count(*) FROM ranobe_glavi WHERE title_id=t.title_id) +
                                (SELECT count(*) FROM anime_serii WHERE title_id=t.title_id) AS glava_count,
                                (SELECT avg(rating) FROM user_ratings WHERE title_id=t.title_id) AS avg_rating
                FROM titles t
                WHERE t.name_on_english = ?
                LIMIT 1
            """;
            return jdbcTemplate.queryForObject(sql, (rs, rowNum)-> {
                return new Object[]{
                        rs.getLong("title_id"),
                        rs.getString("name_on_english"),
                        rs.getString("name_on_russian"),
                        rs.getString("description"),
                        rs.getInt("year"),
                        rs.getString("cover"),
                        rs.getString("genres"),
                        rs.getInt("amount_of_views"),
                        rs.getInt("glava_count"),
                        rs.getDouble("avg_rating")
                };
            }, titleEnglish);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * add_glava(title_id, nomer_glavi, glava, images)
     */
    public void addGlava(Long titleId, int nomerGlavi, String glava, String images) {
        try {
            String sql = "INSERT INTO glava(title_id, nomer_glavi, glava, images) VALUES(?,?,?,?)";
            jdbcTemplate.update(sql, titleId, nomerGlavi, glava, images);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add_ranobe_glava(title_id, nomer_glavi, glava, content_glavi)
     */
    public void addRanobeGlava(Long titleId, int nomerGlavi, String glava, String content) {
        try {
            String sql = "INSERT INTO ranobe_glavi(title_id, nomer_glavi, glava, content_glavi) VALUES(?,?,?,?)";
            jdbcTemplate.update(sql, titleId, nomerGlavi, glava, content);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add_seriya(title_id, nomer_serii, glava, html_code)
     */
    public void addSeriya(Long titleId, int nomerSerii, String seriyaName, String html) {
        try {
            String sql = "INSERT INTO anime_serii(title_id, nomer_glavi, glava, html_code) VALUES(?,?,?,?)";
            jdbcTemplate.update(sql, titleId, nomerSerii, seriyaName, html);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getGlavi(title_id) -> список [номер, glava], объединяя манга/ранобэ/аниме?
     * В Python-коде различали тип, но тут можно вернуть только из нужной таблицы.
     * Или делаете отдельный метод.
     */
    public List<Object[]> getGlavi(Long titleId, String type) {
        // Если type=="манга", вытаскиваем из glava, если "ранобе" — из ranobe_glavi, если "аниме" — из anime_serii
        List<Object[]> result = new ArrayList<>();
        try {
            String sql;
            switch(type) {
                case "манга":
                    sql = "SELECT nomer_glavi, glava FROM glava WHERE title_id=? ORDER BY nomer_glavi DESC";
                    break;
                case "ранобе":
                    sql = "SELECT nomer_glavi, glava FROM ranobe_glavi WHERE title_id=? ORDER BY nomer_glavi DESC";
                    break;
                case "аниме":
                    sql = "SELECT nomer_glavi, glava FROM anime_serii WHERE title_id=? ORDER BY nomer_glavi DESC";
                    break;
                default:
                    return result;
            }
            result = jdbcTemplate.query(sql, (rs, rowNum)-> new Object[]{rs.getInt(1), rs.getString(2)}, titleId);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * showGlava(nazvanie, glava_number) — аналог showGlava
     * Возвращает массив [imagesOrContent, title_id, name_on_russian, glavaName]
     */
    public Object[] showGlava(String nazvanie, int nomerGlavi) {
        try {
            // Сначала узнаём type
            String type = getType( getTitleIdByName(nazvanie) );
            String sql;
            switch(type) {
                case "манга":
                    sql = """
                        SELECT g.images, t.title_id, t.name_on_russian, g.glava
                        FROM glava g
                        JOIN titles t ON t.title_id = g.title_id
                        WHERE t.name_on_english=? AND g.nomer_glavi=?
                        LIMIT 1
                    """;
                    break;
                case "ранобе":
                    sql = """
                        SELECT rg.content_glavi, t.title_id, t.name_on_russian, rg.glava
                        FROM ranobe_glavi rg
                        JOIN titles t ON t.title_id = rg.title_id
                        WHERE t.name_on_english=? AND rg.nomer_glavi=?
                        LIMIT 1
                    """;
                    break;
                case "аниме":
                    sql = """
                        SELECT a.html_code, t.title_id, t.name_on_russian, a.glava
                        FROM anime_serii a
                        JOIN titles t ON t.title_id = a.title_id
                        WHERE t.name_on_english=? AND a.nomer_glavi=?
                        LIMIT 1
                    """;
                    break;
                default:
                    return null;
            }
            return jdbcTemplate.queryForObject(sql, (rs, rowNum)-> new Object[]{
                    rs.getString(1),  // images / content / html
                    rs.getLong(2),    // title_id
                    rs.getString(3),  // name_on_russian
                    rs.getString(4)   // glava
            }, nazvanie, nomerGlavi);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getContentGlaviRanobe(Long titleId, int nomerGlavi) {
        String sql = "SELECT content_glavi FROM ranobe_glavi WHERE title_id = ? AND nomer_glavi = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String[] content = (String[]) rs.getArray("content_glavi").getArray();
                return List.of(content);
            }, titleId, nomerGlavi);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * getLastGlava(nazvanie) — вернуть max(nomer_glavi)
     */
    public Integer getLastGlava(String nazvanie) {
        try {
            Long titleId = getTitleIdByName(nazvanie);
            String type = getType(titleId);
            String sql;
            switch(type) {
                case "манга":
                    sql = "SELECT max(nomer_glavi) FROM glava WHERE title_id=?";
                    break;
                case "ранобе":
                    sql = "SELECT max(nomer_glavi) FROM ranobe_glavi WHERE title_id=?";
                    break;
                case "аниме":
                    sql = "SELECT max(nomer_glavi) FROM anime_serii WHERE title_id=?";
                    break;
                default:
                    return null;
            }
            return jdbcTemplate.queryForObject(sql, Integer.class, titleId);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get_numerized(список тайтлов) — в Python вы брали  i[0] + ";" + numerize(i[4])...
     * Но это логика представления. Можно сделать метод, который вернёт список строк
     */
    public List<String> getNumerized(List<Object[]> titles) {
        // titles — что угодно, например (name_on_rus, cover, name_on_eng, genres, amount_of_views, ...)
        // Возвращаем List<String>, где "titleName;1K"
        List<String> result = new ArrayList<>();
        for(Object[] row : titles) {
            // Предположим, row[0] = name, row[4] = amount_of_views
            String name = (String) row[0];
            int views = Integer.parseInt(String.valueOf(row[4]));
            result.add(name + ";" + numerize(views));
        }
        return result;
    }

    /**
     * getAllTitles() — Возвращаем список [name_on_russian, cover, name_on_english, genres, amount_of_views, ...]
     * В Python вы делали SELECT * FROM titles_for_zapros..., здесь можно сделать raw SELECT
     */
    public List<Title> getAllTitles() {
        String sql = "SELECT * FROM titles";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Title t = new Title();
            t.setTitleId(rs.getLong("title_id"));
            t.setNameOnRussian(rs.getString("name_on_russian"));
            t.setNameOnEnglish(rs.getString("name_on_english"));
            t.setDescription(rs.getString("description"));
            t.setYear(rs.getInt("year"));
            t.setCover(rs.getString("cover"));
            t.setGenres(rs.getString("genres"));
            t.setAmountOfViews(rs.getInt("amount_of_views"));
            t.setType(rs.getString("type"));
            return t;
        });
    }

    /**
     * getMostPopularTitles() — limit 10
     */
    public List<Title> getMostPopularTitles() {
        String sql = """
        SELECT t.title_id,
               t.name_on_russian,
               t.name_on_english,
               t.description,
               t.year,
               t.cover,
               t.genres,
               t.amount_of_views,
               t.type,
               (
                 COALESCE((SELECT count(*) FROM glava        g WHERE g.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM ranobe_glavi r WHERE r.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM anime_serii  a WHERE a.title_id = t.title_id), 0)
               ) AS glava_count,
               (SELECT avg(rating) FROM user_ratings WHERE user_ratings.title_id = t.title_id) AS avg_rating
        FROM titles t
        ORDER BY amount_of_views DESC
        LIMIT 10
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Title title = new Title();
            title.setTitleId(rs.getLong("title_id"));
            title.setNameOnRussian(rs.getString("name_on_russian"));
            title.setNameOnEnglish(rs.getString("name_on_english"));
            title.setDescription(rs.getString("description"));
            title.setYear(rs.getInt("year"));
            title.setCover(rs.getString("cover"));
            title.setGenres(rs.getString("genres"));
            title.setAmountOfViews(rs.getInt("amount_of_views"));
            title.setType(rs.getString("type"));

            title.setGlavaCount(rs.getInt("glava_count"));
            double ratingValue = rs.getDouble("avg_rating");
            if (rs.wasNull()) {
                title.setRating(null);
            } else {
                title.setRating(ratingValue);
            }
            return title;
        });
    }


    /**
     * getComments(title_name, glava_number)
     * Возвращает список [username, profile_pic, comment_text, comment_date, comment_id]
     */
    public List<Object[]> getComments(String titleName, int glavaNumber) {
        try {
            String sql = """
                SELECT u.username, u.profile_pic, c.comment_text, c.comment_date, c.comment_id
                FROM comments c
                JOIN users u ON u.id = c.user_id
                WHERE c.title_name=? AND c.nomer_glavi=?
            """;
            return jdbcTemplate.query(sql, (rs, rowNum)-> new Object[]{
                    rs.getString("username"),
                    rs.getString("profile_pic"),
                    rs.getString("comment_text"),
                    rs.getString("comment_date"),
                    rs.getLong("comment_id")
            }, titleName, glavaNumber);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * addComment(title_name, glava_number, user_id, comment_text, comment_date)
     */
    public void addComment(String titleName, int glavaNumber, Long userId, String commentText, String commentDate) {
        try {
            String sql = """
                INSERT INTO comments(title_name, nomer_glavi, user_id, comment_text, comment_date)
                VALUES(?,?,?,?,?)
            """;
            jdbcTemplate.update(sql, titleName, glavaNumber, userId, commentText, commentDate);
            // дополнительные ачивки
            comAchievement(userId);
            addExp(userId,10);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add_view(title_name_on_english)
     * Возвращает новое количество просмотров
     */
    public int addView(String titleEnglish) {
        try {
            String checkSql = "SELECT title_id, amount_of_views FROM titles WHERE name_on_english=? LIMIT 1";
            Map<String,Object> row = jdbcTemplate.queryForMap(checkSql, titleEnglish);
            Long titleId = ((Number) row.get("title_id")).longValue();
            int oldViews = ((Number) row.get("amount_of_views")).intValue();
            int newViews = oldViews + 1;
            String updateSql = "UPDATE titles SET amount_of_views=? WHERE title_id=?";
            jdbcTemplate.update(updateSql, newViews, titleId);
            return newViews;
        } catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<Title> getAllTitlesWithStats() {
        // Здесь одним запросом достаём:
        //   - все поля из titles t.*
        //   - (SELECT count(*)...) AS glava_count
        //   - (SELECT avg(rating)...) AS rating
        //
        // Если у вас есть 3 таблицы (glava, ranobe_glavi, anime_serii),
        // то складываем count(*) из каждой, чтобы получить общее кол-во глав.
        String sql = """
        SELECT t.title_id,
               t.name_on_russian,
               t.name_on_english,
               t.description,
               t.year,
               t.cover,
               t.genres,
               t.amount_of_views,
               t.type,
               (
                 COALESCE((SELECT count(*) FROM glava        g WHERE g.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM ranobe_glavi r WHERE r.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM anime_serii  a WHERE a.title_id = t.title_id), 0)
               ) AS glava_count,
               (SELECT avg(rating) FROM user_ratings WHERE user_ratings.title_id = t.title_id) AS avg_rating
        FROM titles t
        ORDER BY t.amount_of_views DESC
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Title title = new Title();
            title.setTitleId(rs.getLong("title_id"));
            title.setNameOnRussian(rs.getString("name_on_russian"));
            title.setNameOnEnglish(rs.getString("name_on_english"));
            title.setDescription(rs.getString("description"));
            title.setYear(rs.getInt("year"));
            title.setCover(rs.getString("cover"));
            title.setGenres(rs.getString("genres"));
            title.setAmountOfViews(rs.getInt("amount_of_views"));
            title.setType(rs.getString("type"));

            // Присваиваем вычисленные поля
            title.setGlavaCount(rs.getInt("glava_count"));
            // Может быть null, если нет оценок → rs.getDouble(...) даст 0.0 при отсутствии
            // Поэтому лучше проверять rs.wasNull() или брать Double
            double ratingValue = rs.getDouble("avg_rating");
            if (rs.wasNull()) {
                title.setRating(null); // или 0.0
            } else {
                title.setRating(ratingValue);
            }

            return title;
        });
    }

    /**
     * poisk(nazvanie) — поиск по name_on_russian
     * Возвращает такой же список, как getAllTitles (упрощённо)
     */
    public List<Object[]> poisk(String nazvanie) {
        try {
            String sql = """
                SELECT name_on_russian, cover, name_on_english, genres, amount_of_views,
                       (SELECT count(*) FROM glava WHERE title_id=t.title_id) +
                       (SELECT count(*) FROM ranobe_glavi WHERE title_id=t.title_id) +
                       (SELECT count(*) FROM anime_serii WHERE title_id=t.title_id) AS glava_count,
                       year,
                       description,
                       (SELECT avg(rating) FROM user_ratings WHERE title_id=t.title_id) as average_rating,
                       type
                FROM titles t
                WHERE lower(t.name_on_russian) LIKE ?
            """;
            String param = "%" + nazvanie.toLowerCase() + "%";
            return jdbcTemplate.query(sql, (rs, rowNum)-> new Object[]{
                    rs.getString("name_on_russian"),
                    rs.getString("cover"),
                    rs.getString("name_on_english"),
                    rs.getString("genres"),
                    rs.getInt("amount_of_views"),
                    rs.getInt("glava_count"),
                    rs.getInt("year"),
                    rs.getString("description"),
                    rs.getDouble("average_rating"),
                    rs.getString("type")
            }, param);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * add_to_favorites(user_id, title_id)
     */
    public boolean addToFavorites(Long userId, Long titleId) {
        try {
            // Проверяем, нет ли уже
            String checkSql = "SELECT COUNT(*) FROM favourites WHERE user_id=? AND title_id=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, titleId);
            if(count > 0) {
                return false; // уже есть
            }
            String insertSql = "INSERT INTO favourites(user_id, title_id) VALUES(?,?)";
            jdbcTemplate.update(insertSql, userId, titleId);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * remove_from_favorites(user_id, title_id)
     */
    public boolean removeFromFavorites(Long userId, Long titleId) {
        try {
            String sql = "DELETE FROM favourites WHERE user_id=? AND title_id=?";
            jdbcTemplate.update(sql, userId, titleId);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * title_in_favorites(user_id, title_id)
     */
    public boolean titleInFavorites(Long userId, Long titleId) {
        try {
            String sql = "SELECT COUNT(*) FROM favourites WHERE user_id=? AND title_id=?";
            int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, titleId);
            return count > 0;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get_user_favorites(user_id) -> List<String> (name_on_english)
     */
    public List<String> getUserFavorites(Long userId) {
        try {
            String sql = """
                SELECT t.name_on_english
                FROM favourites f
                JOIN titles t ON t.title_id = f.title_id
                WHERE f.user_id=?
            """;
            return jdbcTemplate.queryForList(sql, String.class, userId);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * get_title_id_by_name(name_on_english)
     */
    public Long getTitleIdByName(String nameOnEnglish) {
        try {
            String sql = "SELECT title_id FROM titles WHERE name_on_english=? LIMIT 1";
            return jdbcTemplate.queryForObject(sql, Long.class, nameOnEnglish);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * add_last_readed_chapter(user_id, title_id, nomer_glavi)
     */
    public void addLastReadedChapter(Long userId, Long titleId, int nomerGlavi) {
        try {
            // Проверяем, есть ли уже запись
            String checkSql = "SELECT COUNT(*) FROM user_reading_history WHERE user_id=? AND title_id=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, titleId);
            if(count>0) {
                // сравним last_chapter_read
                String selectSql = "SELECT last_chapter_read FROM user_reading_history WHERE user_id=? AND title_id=?";
                Integer oldVal = jdbcTemplate.queryForObject(selectSql, Integer.class, userId, titleId);
                if(oldVal!=null && nomerGlavi>oldVal) {
                    String updateSql = "UPDATE user_reading_history SET last_chapter_read=? WHERE user_id=? AND title_id=?";
                    jdbcTemplate.update(updateSql, nomerGlavi, userId, titleId);
                }
            } else {
                String insertSql = "INSERT INTO user_reading_history(user_id, title_id, last_chapter_read) VALUES(?,?,?)";
                jdbcTemplate.update(insertSql, userId, titleId, nomerGlavi);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get_last_readed_chapter(user_id, title_id) -> Integer
     */
    public Integer getLastReadedChapter(Long userId, Long titleId) {
        try {
            String sql = "SELECT last_chapter_read FROM user_reading_history WHERE user_id=? AND title_id=?";
            return jdbcTemplate.queryForObject(sql, Integer.class, userId, titleId);
        } catch(Exception e) {
            // возможно EmptyResultDataAccessException
            return null;
        }
    }

    /**
     * user_chapter_status(user_id, title_id, nomer_glavi)
     */
    public void userChapterStatus(Long userId, Long titleId, int nomerGlavi) {
        try {
            String checkSql = "SELECT COUNT(*) FROM user_chapter_status WHERE user_id=? AND title_id=? AND nomer_glavi=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, titleId, nomerGlavi);
            if(count==0) {
                String insertSql = "INSERT INTO user_chapter_status(user_id, title_id, nomer_glavi, is_read) VALUES(?,?,?,TRUE)";
                jdbcTemplate.update(insertSql, userId, titleId, nomerGlavi);
            }
            // Проверяем, не прочитал ли он весь тайтл (add_full_read_title)
            addFullReadTitle(userId, titleId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get_readed_chapters(user_id, title_id)
     */
    public List<Integer> getReadedChapters(Long userId, Long titleId) {
        try {
            String sql = "SELECT nomer_glavi FROM user_chapter_status WHERE user_id=? AND title_id=?";
            return jdbcTemplate.queryForList(sql, Integer.class, userId, titleId);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * add_full_read_title(user_id, title_id)
     */
    public void addFullReadTitle(Long userId, Long titleId) {
        try {
            // проверяем, есть ли уже в users_full_title_read
            String checkSql = "SELECT COUNT(*) FROM users_full_title_read WHERE user_id=? AND title_id=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, titleId);
            if(count==0) {
                // Сколько всего глав?
                String glavaCountSql = "SELECT COUNT(*) FROM glava WHERE title_id=?";
                int mangaCount = jdbcTemplate.queryForObject(glavaCountSql, Integer.class, titleId);

                // (Если хотите учесть и ранобэ/аниме, аналогично сложить)
                // Но в Python-коде учитывалось только glava?
                // Либо нужно суммировать: manga + ranobe + anime
                // Для простоты:
                String ranobeCountSql = "SELECT COUNT(*) FROM ranobe_glavi WHERE title_id=?";
                int ranobeCount = jdbcTemplate.queryForObject(ranobeCountSql, Integer.class, titleId);
                String animeCountSql = "SELECT COUNT(*) FROM anime_serii WHERE title_id=?";
                int animeCount = jdbcTemplate.queryForObject(animeCountSql, Integer.class, titleId);

                int totalCount = mangaCount + ranobeCount + animeCount;

                // Сколько прочитал?
                String userReadSql = "SELECT COUNT(*) FROM user_chapter_status WHERE user_id=? AND title_id=?";
                int userRead = jdbcTemplate.queryForObject(userReadSql, Integer.class, userId, titleId);

                if(totalCount == userRead && totalCount!=0) {
                    // добавляем запись
                    String insertSql = "INSERT INTO users_full_title_read(user_id, title_id) VALUES(?,?)";
                    jdbcTemplate.update(insertSql, userId, titleId);
                    // +1000 exp
                    addExp(userId,1000);
                }
            }
            // Проверяем количество прочитанных тайтлов (для ачивки)
            String countSql = "SELECT COUNT(*) FROM users_full_title_read WHERE user_id=?";
            int howMany = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
            if(howMany!=0) {
                // если user_achievements есть(3) -> upgrade to(4) etc...
                // Для простоты опустим
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add_rating_to_title(user_id, title_id, rating)
     */
    public boolean addRatingToTitle(Long userId, Long titleId, double rating) {
        try {
            String checkSql = "SELECT COUNT(*) FROM user_ratings WHERE user_id=? AND title_id=?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, titleId);
            if(count>0) {
                // update
                String updSql = "UPDATE user_ratings SET rating=? WHERE user_id=? AND title_id=?";
                jdbcTemplate.update(updSql, rating, userId, titleId);
            } else {
                // insert
                String insSql = "INSERT INTO user_ratings(user_id, title_id, rating) VALUES(?,?,?)";
                jdbcTemplate.update(insSql, userId, titleId, rating);
                addExp(userId,100); // как в Python
            }
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * random_manga() -> возвращает name_on_english случайного тайтла
     */
    public String randomManga() {
        try {
            String sql = "SELECT name_on_english FROM titles ORDER BY RANDOM() LIMIT 1";
            return jdbcTemplate.queryForObject(sql, String.class);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * show_users_achievements(user_id)
     * Возвращает список [achievemnt_name, achievement_desc, achievement_image]
     */
    public List<Object[]> showUsersAchievements(Long userId) {
        try {
            String sql = """
                SELECT a.achievemnt_name, a.achievement_desc, a.achievement_image
                FROM user_achievements ua
                JOIN achievements a ON a.achievement_id = ua.achievement_id
                WHERE ua.user_id=?
            """;
            return jdbcTemplate.query(sql, (rs, rowNum)-> new Object[]{
                    rs.getString("achievemnt_name"),
                    rs.getString("achievement_desc"),
                    rs.getString("achievement_image")
            }, userId);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * com_achievement(user_id) -> проверка кол-ва комментариев, добавление ачивки
     */
    public void comAchievement(Long userId) {
        try {
            String cntSql = "SELECT COUNT(*) FROM comments WHERE user_id=?";
            int commentCount = jdbcTemplate.queryForObject(cntSql, Integer.class, userId);
            String checkSql = "SELECT achievement_id FROM user_achievements WHERE user_id=? AND achievement_id IN(1,2)";
            List<Integer> have = jdbcTemplate.queryForList(checkSql, Integer.class, userId);
            if(!have.isEmpty()) {
                // уже есть achievement_id=1 или 2
                // если у нас 1 и commentCount>=5, -> upgrade to 2
                if(commentCount>=5 && have.contains(1)) {
                    String upd = "UPDATE user_achievements SET achievement_id=2 WHERE user_id=? AND achievement_id=1";
                    jdbcTemplate.update(upd, userId);
                }
            } else {
                // нет ачивки 1/2, => даём 1
                String ins = "INSERT INTO user_achievements(achievement_id, user_id) VALUES(1, ?)";
                jdbcTemplate.update(ins, userId);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * glava_achievement(user_id) -> аналог
     */
    public void glavaAchievement(Long userId) {
        try {
            // SELECT COUNT(*) FROM user_reading_history
            String sql = "SELECT COUNT(*) FROM user_reading_history WHERE user_id=?";
            int readCount = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            // check user_achievements
            String check = "SELECT achievement_id FROM user_achievements WHERE user_id=? AND achievement_id IN(5,6,7)";
            List<Integer> have = jdbcTemplate.queryForList(check, Integer.class, userId);
            if(!have.isEmpty()) {
                // если есть 5, а readCount>=100 => update to 6
                // если есть 6, а readCount>=500 => update to 7
                if(have.contains(5) && readCount>=100 && readCount<500) {
                    String upd = "UPDATE user_achievements SET achievement_id=6 WHERE user_id=? AND achievement_id=5";
                    jdbcTemplate.update(upd, userId);
                } else if(have.contains(6) && readCount>=500) {
                    String upd = "UPDATE user_achievements SET achievement_id=7 WHERE user_id=? AND achievement_id=6";
                    jdbcTemplate.update(upd, userId);
                }
            } else {
                // если readCount>=10 => INSERT (5)
                if(readCount>=10) {
                    String ins = "INSERT INTO user_achievements(achievement_id, user_id) VALUES(5,?)";
                    jdbcTemplate.update(ins, userId);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get_last_updated_titles()
     * (в Python: SELECT ... ORDER BY last_update DESC LIMIT 5)
     */
    public List<Title> getLastUpdatedTitles() {
        String sql = """
        SELECT t.title_id,
               t.name_on_russian,
               t.name_on_english,
               t.description,
               t.year,
               t.cover,
               t.genres,
               t.amount_of_views,
               t.type,
               (
                 COALESCE((SELECT count(*) FROM glava        g WHERE g.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM ranobe_glavi r WHERE r.title_id = t.title_id), 0) +
                 COALESCE((SELECT count(*) FROM anime_serii  a WHERE a.title_id = t.title_id), 0)
               ) AS glava_count,
               (SELECT avg(rating) FROM user_ratings WHERE user_ratings.title_id = t.title_id) AS avg_rating,
               GREATEST(
                 COALESCE((SELECT max(last_update) FROM glava        gg WHERE gg.title_id = t.title_id), '1900-01-01'),
                 COALESCE((SELECT max(last_update) FROM ranobe_glavi rr WHERE rr.title_id = t.title_id), '1900-01-01'),
                 COALESCE((SELECT max(last_update) FROM anime_serii  aa WHERE aa.title_id = t.title_id), '1900-01-01')
               ) AS last_update
        FROM titles t
        ORDER BY last_update DESC
        LIMIT 5
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Title title = new Title();
            title.setTitleId(rs.getLong("title_id"));
            title.setNameOnRussian(rs.getString("name_on_russian"));
            title.setNameOnEnglish(rs.getString("name_on_english"));
            title.setDescription(rs.getString("description"));
            title.setYear(rs.getInt("year"));
            title.setCover(rs.getString("cover"));
            title.setGenres(rs.getString("genres"));
            title.setAmountOfViews(rs.getInt("amount_of_views"));
            title.setType(rs.getString("type"));

            title.setGlavaCount(rs.getInt("glava_count"));
            double ratingValue = rs.getDouble("avg_rating");
            if (rs.wasNull()) {
                title.setRating(null);
            } else {
                title.setRating(ratingValue);
            }
            return title;
        });
    }


    /**
     * get_all_users(исключая текущего?)
     */
    public List<Map<String,Object>> getAllUsers(Long currentUserId) {
        try {
            String sql = "SELECT * FROM users WHERE id <> ?";
            return jdbcTemplate.queryForList(sql, currentUserId);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * change_status(username, status)
     */
    public void changeStatus(String username, String status) {
        try {
            String sql = "UPDATE users SET status=? WHERE username=?";
            jdbcTemplate.update(sql, status, username);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete_comment(comment_id)
     */
    public void deleteComment(Long commentId) {
        try {
            String sql = "DELETE FROM comments WHERE comment_id=?";
            jdbcTemplate.update(sql, commentId);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get_type(title_id) -> возвращает 'манга'/'ранобе'/'аниме' и т.д.
     */
    public String getType(Long titleId) {
        try {
            String sql = "SELECT type FROM titles WHERE title_id=?";
            return jdbcTemplate.queryForObject(sql, String.class, titleId);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get_uniq_genres()
     * SELECT DISTINCT unnest(string_to_array(genres, ';')) FROM titles
     */
    public List<String> getUniqGenres() {
        try {
            String sql = "SELECT DISTINCT unnest(string_to_array(genres, ';')) AS unique_genre FROM titles";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * get_titles_with_genres(selected_genres, chapter_start, chapter_end, year_start, year_end, title_types)
     * Это фильтрованный поиск
     */
    public List<Object[]> getTitlesWithGenres(List<String> selectedGenres,
                                              int chapterStart, int chapterEnd,
                                              int yearStart, int yearEnd,
                                              List<String> titleTypes) {
        // Для упрощения: "WHERE glava_count BETWEEN ? AND ? AND year BETWEEN ? AND ? AND type IN(...)"
        // а потом пост-фильтруем те, у кого все selectedGenres входят в genres
        try {
            String inPlaceholders = String.join(",", Collections.nCopies(titleTypes.size(), "?"));
            String sql = """
                SELECT t.name_on_russian, t.cover, t.name_on_english, t.genres, t.amount_of_views,
                       (COALESCE((SELECT count(*) FROM glava g WHERE g.title_id=t.title_id),0) +
                        COALESCE((SELECT count(*) FROM ranobe_glavi rg WHERE rg.title_id=t.title_id),0) +
                        COALESCE((SELECT count(*) FROM anime_serii aa WHERE aa.title_id=t.title_id),0)) as glava_count,
                       t.year,
                       t.description,
                       (SELECT avg(rating) FROM user_ratings ur WHERE ur.title_id=t.title_id) as average_rating,
                       t.type
                FROM titles t
                -- здесь пока не фильтруем glava_count, year, type
            """;

            // Сперва достанем все тайтлы, потом вручную отфильтруем
            List<Object[]> allTitles = jdbcTemplate.query(sql, (rs, rowNum)-> new Object[]{
                    rs.getString("name_on_russian"),
                    rs.getString("cover"),
                    rs.getString("name_on_english"),
                    rs.getString("genres"),
                    rs.getInt("amount_of_views"),
                    rs.getInt("glava_count"),
                    rs.getInt("year"),
                    rs.getString("description"),
                    rs.getDouble("average_rating"),
                    rs.getString("type")
            });

            // Фильтрация в памяти (или можно было сразу генерировать сложный WHERE)
            List<Object[]> result = new ArrayList<>();
            for(Object[] row : allTitles) {
                int glavaCount = (Integer) row[5];
                int year = (Integer) row[6];
                String type = (String) row[9];
                if(glavaCount >= chapterStart && glavaCount <= chapterEnd &&
                        year >= yearStart && year <= yearEnd &&
                        titleTypes.contains(type))
                {
                    // проверяем жанры
                    String genres = (String) row[3];
                    List<String> splitted = Arrays.asList(genres.split(";"));
                    boolean allOk = true;
                    for(String sg : selectedGenres) {
                        if(!splitted.contains(sg)) {
                            allOk = false;
                            break;
                        }
                    }
                    if(allOk) {
                        result.add(row);
                    }
                }
            }
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * find_user_recommendations(user_id)
     * В Python у вас была логика "если есть фавориты, взять их жанры", etc...
     * Ниже — упрощённый пример
     */
    public List<Object[]> findUserRecommendations(Long userId) {
        // Проверяем, есть ли у пользователя фавориты
        try {
            String cntSql = "SELECT COUNT(*) FROM favourites WHERE user_id=?";
            int count = jdbcTemplate.queryForObject(cntSql, Integer.class, userId);
            if(count==0) {
                return Collections.emptyList();
            }
            // Всё, что ниже, будет упрощённо. Вы качали все тайтлы, смотрели общие жанры...
            // Для демонстрации сделаем коротко:
            String sqlAll = """
                SELECT t.name_on_russian, t.cover, t.name_on_english, t.genres, t.amount_of_views,
                       (SELECT count(*) FROM glava gg WHERE gg.title_id=t.title_id) +
                       (SELECT count(*) FROM ranobe_glavi rr WHERE rr.title_id=t.title_id) +
                       (SELECT count(*) FROM anime_serii aa WHERE aa.title_id=t.title_id) AS glava_count,
                       t.year,
                       t.description,
                       (SELECT avg(rating) FROM user_ratings ur WHERE ur.title_id=t.title_id) as average_rating,
                       t.type
                FROM titles t
            """;
            List<Object[]> allTitles = jdbcTemplate.query(sqlAll, (rs, rowNum)-> new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getInt(6),
                    rs.getInt(7),
                    rs.getString(8),
                    rs.getDouble(9),
                    rs.getString(10)
            });
            // Фавориты пользователя
            String sqlFav = """
                SELECT t.name_on_russian, t.cover, t.name_on_english, t.genres, t.amount_of_views,
                       (SELECT count(*) FROM glava gg WHERE gg.title_id=t.title_id) +
                       (SELECT count(*) FROM ranobe_glavi rr WHERE rr.title_id=t.title_id) +
                       (SELECT count(*) FROM anime_serii aa WHERE aa.title_id=t.title_id) AS glava_count,
                       t.year,
                       t.description,
                       (SELECT avg(rating) FROM user_ratings ur WHERE ur.title_id=t.title_id) as average_rating,
                       t.type
                FROM favourites f
                JOIN titles t ON t.title_id=f.title_id
                WHERE f.user_id=?
            """;
            List<Object[]> userFavorites = jdbcTemplate.query(sqlFav, (rs, rowNum)-> new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getInt(6),
                    rs.getInt(7),
                    rs.getString(8),
                    rs.getDouble(9),
                    rs.getString(10)
            }, userId);
            Set<String> userGenres = new HashSet<>();
            Set<String> userFavTitles = new HashSet<>();
            for(Object[] row : userFavorites) {
                String rusName = (String) row[0];
                userFavTitles.add(rusName);
                String allG = (String) row[3];
                if(allG!=null) {
                    userGenres.addAll(Arrays.asList(allG.split(";")));
                }
            }
            // Считаем "очки" = кол-во общих жанров
            Map<Object[], Integer> scores = new HashMap<>();
            for(Object[] row : allTitles) {
                String rusName = (String) row[0];
                if(userFavTitles.contains(rusName)) {
                    continue; // уже в фаворитах
                }
                String g = (String) row[3];
                if(g == null) g = "";
                Set<String> gens = new HashSet<>(Arrays.asList(g.split(";")));
                gens.retainAll(userGenres);  // пересечение
                scores.put(row, gens.size());
            }
            // Сортируем по убыванию
            List<Map.Entry<Object[], Integer>> list = new ArrayList<>(scores.entrySet());
            list.sort((a,b)->Integer.compare(b.getValue(), a.getValue()));
            // Берём первые 10
            List<Object[]> recommendations = new ArrayList<>();
            for(int i=0; i<Math.min(10, list.size()); i++) {
                recommendations.add(list.get(i).getKey());
            }
            return recommendations;
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    /**
     * find_title_recommendations(title_id)
     * Аналог: смотрим жанры тайтла, ищем похожие
     */
    public List<Title> findTitleRecommendations(Long titleId) {
        try {
            // SQL-запрос для получения всех записей
            String sqlAll = """
            SELECT t.title_id, t.name_on_russian, t.cover, t.name_on_english, t.genres, t.amount_of_views,
                   (SELECT count(*) FROM glava gg WHERE gg.title_id = t.title_id) +
                   (SELECT count(*) FROM ranobe_glavi rr WHERE rr.title_id = t.title_id) +
                   (SELECT count(*) FROM anime_serii aa WHERE aa.title_id = t.title_id) AS glava_count,
                   t.year, t.description,
                   (SELECT avg(rating) FROM user_ratings ur WHERE ur.title_id = t.title_id) AS average_rating,
                   t.type
            FROM titles t
        """;

            // Извлечение всех заголовков
            List<Title> allTitles = jdbcTemplate.query(sqlAll, (rs, rowNum) -> {
                Title title = new Title();
                title.setTitleId(rs.getLong("title_id"));
                title.setNameOnRussian(rs.getString("name_on_russian"));
                title.setCover(rs.getString("cover"));
                title.setNameOnEnglish(rs.getString("name_on_english"));
                title.setGenres(rs.getString("genres"));
                title.setAmountOfViews(rs.getInt("amount_of_views"));
                title.setGlavaCount(rs.getInt("glava_count"));
                title.setYear(rs.getInt("year"));
                title.setDescription(rs.getString("description"));
                double ratingValue = rs.getDouble("average_rating");
                if (rs.wasNull()) {
                    title.setRating(null);
                } else {
                    title.setRating(ratingValue);
                }
                title.setType(rs.getString("type"));
                return title;
            });

            // Поиск выбранного заголовка
            Title selected = null;
            for (Title title : allTitles) {
                if (title.getTitleId().equals(titleId)) {
                    selected = title;
                    break;
                }
            }
            if (selected == null) return Collections.emptyList();

            // Получение жанров выбранного заголовка
            Set<String> selectedGenresSet = new HashSet<>(Arrays.asList(selected.getGenres().split(";")));

            // Подсчёт количества общих жанров
            Map<Title, Integer> scores = new HashMap<>();
            for (Title title : allTitles) {
                if (title.getTitleId().equals(titleId)) continue;
                Set<String> currentGenresSet = new HashSet<>(Arrays.asList(title.getGenres().split(";")));
                currentGenresSet.retainAll(selectedGenresSet);
                scores.put(title, currentGenresSet.size());
            }

            // Сортировка по количеству совпадающих жанров
            List<Map.Entry<Title, Integer>> sortedEntries = new ArrayList<>(scores.entrySet());
            sortedEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            // Формирование списка рекомендаций (топ-10)
            List<Title> recommendations = new ArrayList<>();
            for (int i = 0; i < Math.min(10, sortedEntries.size()); i++) {
                recommendations.add(sortedEntries.get(i).getKey());
            }

            return recommendations;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // В FDATABASEService (или другом сервисе), напишем:
    public List<ChapterDto> getGlaviAsDto(Long titleId, String type) {
        // В зависимости от type -> манга, ранобэ, аниме
        String sql;
        switch (type) {
            case "манга":
                sql = "SELECT nomer_glavi, glava FROM glava WHERE title_id=? ORDER BY nomer_glavi DESC";
                break;
            case "ранобе":
                sql = "SELECT nomer_glavi, glava FROM ranobe_glavi WHERE title_id=? ORDER BY nomer_glavi DESC";
                break;
            case "аниме":
                sql = "SELECT nomer_glavi, glava FROM anime_serii WHERE title_id=? ORDER BY nomer_glavi DESC";
                break;
            default:
                // пустой список
                return Collections.emptyList();
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ChapterDto dto = new ChapterDto();
            dto.setNomerGlavi(rs.getInt("nomer_glavi"));
            dto.setGlavaName(rs.getString("glava"));
            return dto;
        }, titleId);
    }


}
