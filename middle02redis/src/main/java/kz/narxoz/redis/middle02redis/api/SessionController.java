package kz.narxoz.redis.middle02redis.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kz.narxoz.redis.middle02redis.dto.SessionDataRequest;
import kz.narxoz.redis.middle02redis.dto.SessionInfo;
import kz.narxoz.redis.middle02redis.dto.SessionLoginRequest;
import kz.narxoz.redis.middle02redis.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
@Validated
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionInfo login(@Valid @RequestBody SessionLoginRequest request, HttpSession session) {
        return sessionService.login(session, request.username());
    }

    @PostMapping("/data")
    public SessionInfo saveData(@Valid @RequestBody SessionDataRequest request, HttpSession session) {
        return sessionService.appendData(session, request.payload());
    }

    @GetMapping("/me")
    public SessionInfo currentSession(HttpSession session) {
        return sessionService.getSessionInfo(session);
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        sessionService.logout(session);
    }
}
