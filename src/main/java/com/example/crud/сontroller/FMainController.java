// src/main/java/com/example/demo/controller/FMainController.java

package com.example.crud.сontroller;

import com.example.crud.dto.ChapterDto;
import com.example.crud.model.Title;
import com.example.crud.repository.TitleRepository;
import com.example.crud.repository.UserRepository;
import com.example.crud.model.Title;
import com.example.crud.model.User;
import com.example.crud.repository.TitleRepository;
import com.example.crud.repository.UserRepository;
import com.example.crud.service.FDATABASEService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FMainController {

    private final FDATABASEService dbService;
    private final TitleRepository titleRepo;
    private final UserRepository userRepo;

    @GetMapping("/")
    public String indexPage(Model model, Authentication auth) {
        // Аналог index() из Flask
        // Все тайтлы
        List<Title> vse = dbService.getAllTitlesWithStats();
        List<Title> mostPopular=dbService.getMostPopularTitles();
        List<Title> lastUpdated=dbService.getLastUpdatedTitles();
        // Пример "нумерации"
        // Получаем amount_of_views -> превращаем в "1K", "2K" и т.д.
        // Для демонстрации делаем простую логику
        System.out.println("ogogooooooo");
//        for (int i = 0; i < vse.size(); i++) {
//            System.out.println(vse.get(i).getAmountOfViews());
//        }
//        for(int i=0;i<mostPopular.size();i++){
//            System.out.println(mostPopular.get(i).getCover());
//        }
        model.addAttribute("titles", vse);
        model.addAttribute("title", "Главная страница");
        model.addAttribute("recommendTitles", Collections.emptyList());
        model.addAttribute("mostPopular", mostPopular);
        model.addAttribute("lastUpdatedTitles",lastUpdated);
        // Допустим, соберём в строку numerized
        // (в Flask вы возвращали массив, тут можно оформить как угодно)
        // ...
        return "mainPage";
    }

    // Обработчик для 404 (вместо @errorhandler(404))
    // Можно сделать через @ControllerAdvice, но для краткости:
    @GetMapping("/404")
    public String pageNotFound() {
        return "pageNotFound";
    }

    // Показ конкретного тайтла
    @GetMapping("/titles/{nazvanie}")
    public String showTitle(@PathVariable String nazvanie, Model model, Authentication auth) {
        // Ищем тайтл
        Optional<Title> opt = titleRepo.findByNameOnEnglish(nazvanie);
        if (opt.isEmpty()) {
            return "pageNotFound";
        }
        Title t = opt.get();
        List<Title> recomendations=dbService.findTitleRecommendations(opt.get().getTitleId());
        // Увеличиваем просмотры
        int updatedViews = dbService.addView(nazvanie);
        model.addAttribute("titleObj", t);
        model.addAttribute("prosmotri", dbService.numerize(updatedViews));
        model.addAttribute("recomend_titles",recomendations);

        // Загружаем главы в DTO-формате
        List<ChapterDto> glavi = dbService.getGlaviAsDto(t.getTitleId(), t.getType());
        model.addAttribute("glavi", glavi);
        System.out.println(glavi.get(0).getGlavaName());
        // Если пользователь залогинен - проверяем избранное, прочитанные главы, lastChapter
        if (auth != null && auth.isAuthenticated()) {
            User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                boolean isFav = dbService.titleInFavorites(currentUser.getId(), t.getTitleId());
                model.addAttribute("isFavorite", isFav);

                List<Integer> readedChapters = dbService.getReadedChapters(currentUser.getId(), t.getTitleId());
                model.addAttribute("readedChapters", readedChapters);

                Integer lastChapter = dbService.getLastReadedChapter(currentUser.getId(), t.getTitleId());
                model.addAttribute("lastChapter", lastChapter);
            }
        } else {
            // Гость (не залогинен)
            model.addAttribute("isFavorite", false);
            model.addAttribute("readedChapters", Collections.emptyList());
            model.addAttribute("lastChapter", null);
        }

        return "primerTitle";
    }


    // Оценка тайтла
    @PostMapping("/titles/{titleId}/rate")
    public String rateTitle(@PathVariable("titleId") Long titleId,
                            @RequestParam("rating") double rating,
                            Authentication auth) {
        // Аналог route('/titles/<int:title_id>/rate', methods=['POST'])
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        // Допустим, вы храните в таблице user_ratings
        // Здесь можно просто сделать insert/update через jdbcTemplate
        // ...
        // dbService.addRatingToTitle(currentUserId, titleId, rating)
        return "redirect:/titles/" + titleId;
    }

    // Показ "главы" / "серии"
    // /titles/{nazvanie}/{nomer_glavi}
    @GetMapping("/titles/{nazvanie}/{nomer_glavi}")
    public String showChapter(@PathVariable("nazvanie") String nazvanie,
                              @PathVariable("nomer_glavi") int nomerGlavi,
                              Model model,
                              Authentication auth) {
        // Проверка, что пользователь залогинен
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // Находим сам Title по nazvanie (что соответствует name_on_english)
        Long titleId = dbService.getTitleIdByName(nazvanie);
        if (titleId == null) {
            return "pageNotFound";
        }
        String type = dbService.getType(titleId);
        if (type == null) {
            return "pageNotFound";
        }

        // Ищем нашего текущего пользователя в базе:
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if (currentUser == null) {
            // на всякий случай
            return "redirect:/login";
        }
        Long userId = currentUser.getId();

        // Логика: проставляем статус "прочитанная глава"
        dbService.userChapterStatus(userId, titleId, nomerGlavi);
        model.addAttribute("currentUser",currentUser);
        // Допустим, нам нужно показать список прочитанных глав:
        var readedChapters = dbService.getReadedChapters(userId, titleId);
        model.addAttribute("readedChapters", readedChapters);

        // Загружаем данные текущей главы
        Object[] glavaData = dbService.showGlava(nazvanie, nomerGlavi);
        if (glavaData == null) {
            return "pageNotFound";
        }
        String contentOrImages = (String) glavaData[0];
        String titleNameRus    = (String) glavaData[2];
        String glavaName       = (String) glavaData[3];

        // Для бокового списка
        var glaviList = dbService.getGlaviAsDto(titleId, type);
        model.addAttribute("glavi", glaviList);

        // Комментарии
        var comments = dbService.getComments(nazvanie, nomerGlavi);
        model.addAttribute("comments", comments);

        // lastGlava (для навигации вперед-назад)
        Integer lastGlava = dbService.getLastGlava(nazvanie);
        model.addAttribute("lastGlava", lastGlava);

        // В модель
        model.addAttribute("nazvanie", nazvanie);
        model.addAttribute("nomer", nomerGlavi);
        model.addAttribute("curGlava", glavaName);
        model.addAttribute("titleName", titleNameRus);
        System.out.println(comments.get(0)[1]);
        // В зависимости от типа (манга/ранобэ/аниме) — разный шаблон
        if (type.equals("манга")) {
            String[] pages = contentOrImages.split(";");
            model.addAttribute("pages", pages);
            return "Glava";
        } else if (type.equals("ранобе")) {
            // Например, разбиваем текст построчно
            String[] contentLines = contentOrImages.split("\n");
            model.addAttribute("contentGlavi", contentLines);
            return "ranobeGlava";
        } else if (type.equals("аниме")) {
            model.addAttribute("htmlCode", contentOrImages);
            return "animeSeriya";
        } else {
            return "pageNotFound";
        }
    }


    // POST запрос для добавления комментария, если он есть
    @PostMapping("/titles/{nazvanie}/{nomer_glavi}")
    public String postComment(@PathVariable String nazvanie,
                              @PathVariable int nomer_glavi,
                              @RequestParam("comment_input") String commentInput,
                              Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        if(commentInput.trim().isEmpty()) {
            // возможно, стоит добавить flash-сообщение
            return "redirect:/titles/" + nazvanie + "/" + nomer_glavi;
        }
        // dbService.addComment(...)
        // ...
        return "redirect:/titles/" + nazvanie + "/" + nomer_glavi;
    }

    // Профиль
    @GetMapping("/profile")
    public String profilePage(Model model, Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if(currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "profile";
    }

    // POST для загрузки аватара / обновления description
    @PostMapping("/profile")
    public String updateProfile(@RequestParam(value="description", required=false) String desc,
                                @RequestParam(value="img", required=false) MultipartFile img,
                                Authentication auth) throws IOException {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if(currentUser == null) {
            return "redirect:/login";
        }
        if(desc != null && !desc.isEmpty()) {
            currentUser.setDescription(desc);
        }
        if(img != null && !img.isEmpty()) {
            // Аналог secure_filename + сохранение
            String originalName = img.getOriginalFilename();
            if(originalName != null) {
                File uploadDir = new File("uploads");
                if(!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                File dest = new File(uploadDir, originalName);
                int counter = 1;
                String baseName = originalName;
                String ext = "";
                if(originalName.contains(".")) {
                    int dotIndex = originalName.lastIndexOf(".");
                    baseName = originalName.substring(0, dotIndex);
                    ext = originalName.substring(dotIndex);
                }
                while(dest.exists()) {
                    dest = new File(uploadDir, baseName + "(" + counter + ")" + ext);
                    counter++;
                }
                img.transferTo(dest);
                // путь
                currentUser.setProfilePic(dest.getPath());
            }
        }
        userRepo.save(currentUser);
        return "redirect:/profile";
    }

    // Просмотр другого пользователя
    @GetMapping("/users/{username}")
    public String showAnotherUser(@PathVariable String username, Model model, Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepo.findByUsername(username).orElse(null);
        if(user == null) {
            return "pageNotFound";
        }
        // Если это мы сами, перекидываем на /profile
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if(currentUser != null && currentUser.getId().equals(user.getId())) {
            return "redirect:/profile";
        }
        model.addAttribute("theUser", user);
        return "user";
    }

    // Поиск (GET/POST)
    @GetMapping("/poisk")
    public String poiskPage() {
        return "poisk";
    }

    @PostMapping("/poisk")
    public String doSearch(@RequestParam("poisk") String poisk, Model model) {
        List<Title> found = dbService.searchByName(poisk);
        model.addAttribute("titles", found);
        return "poisk";
    }

    // Добавить в избранное
    @PostMapping("/add_to_favorite/{titleId}")
    public String addToFavorite(@PathVariable Long titleId, Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if(currentUser == null) return "redirect:/login";
        dbService.addToFavorites(currentUser.getId(), titleId);
        return "redirect:/titles/" + titleId;
    }

    // Тумблер "избранное"
    @PostMapping("/toggle_favorite/{titleId}")
    public String toggleFavorite(@PathVariable Long titleId,
                                 @RequestParam("action") String action,
                                 Authentication auth) {
        if(auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User currentUser = userRepo.findByUsername(auth.getName()).orElse(null);
        if(currentUser == null) return "redirect:/login";

        if("add".equals(action)) {
            dbService.addToFavorites(currentUser.getId(), titleId);
        } else if("remove".equals(action)) {
            dbService.removeFromFavorites(currentUser.getId(), titleId);
        }
        // Возврат на предыдущую страницу
        return "redirect:/titles/" + titleId;
    }

    // Admin
    @GetMapping("/admin")
    public String adminPage(Model model, Authentication auth) {
        // Проверим, что user → Админ (иначе редирект/404)
        if(auth == null || !auth.isAuthenticated()) {
            return "pageNotFound";
        }
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if(user == null || !"Админ".equals(user.getStatus())) {
            return "pageNotFound";
        }
        model.addAttribute("users", userRepo.findAll());
        return "admin";
    }

    // Пример POST на /admin: вы добавляли новую мангу...
    @PostMapping("/admin")
    public String adminAddTitle(@RequestParam("name_on_rus") String rus,
                                @RequestParam("name_on_eng") String eng,
                                @RequestParam("href") String href) {
        // В Flask вы записывали в titli.txt и т.д.
        // Здесь можно сразу сделать insert в БД или запустить скрипт
        // ...
        return "redirect:/admin";
    }

    // Рандомная манга
    @GetMapping("/random_manga")
    public String randomManga() {
        String random = dbService.randomManga();
        return "redirect:/titles/" + random;
    }

    // Тестовый маршрут
    @GetMapping("/test")
    public String testit() {
        return "testit";
    }

    // Смена статуса
    @PostMapping("/changeStatus")
    public String changeStatus(@RequestParam("selectedUser") String username,
                               @RequestParam("selectedStatus") String status) {
        // Аналог dbase.change_status(selected_user, selected_status)
        // Можно через userRepo
        User user = userRepo.findByUsername(username).orElse(null);
        if(user != null) {
            user.setStatus(status);
            userRepo.save(user);
        }
        return "redirect:/admin";
    }

    // Удаление комментария
    @PostMapping("/delete_comment")
    @ResponseBody
    public String deleteComment(@RequestParam("comment_id") Long commentId) {
        dbService.deleteComment(commentId);
        // Возвращаем JSON или что-то
        return "{\"result\":\"ok\"}";
    }
}
