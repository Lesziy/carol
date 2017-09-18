package lesziy.carol.web.ui.dashboard;

import lesziy.carol.domain.lottery.DtoWishGiver;
import lesziy.carol.domain.lottery.LotteryFacade;
import lesziy.carol.domain.user.DtoUser;
import lesziy.carol.domain.user.SystemRole;
import lesziy.carol.domain.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping(value = "/dashboard")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class DashboardController {

    private final LotteryFacade lotteryFacade;

    private final UserFacade userFacade;

    @GetMapping
    public String dashboard(Model model) {
        fillDashboardModel(model);
        return "dashboard";
    }

    private void fillDashboardModel(Model model) {
        DtoUser loggedUser = loggedUser();
        Optional<DtoWishGiver> recipientWishes = lotteryFacade.actualRecipientWishes(loggedUser.id());
        model.addAttribute("isAdmin", loggedUser.systemRole() == SystemRole.ADMIN);
        model.addAttribute("canPerformLottery", lotteryFacade.annualLotteryNotPerformedYet());
        model.addAttribute("myWishes", lotteryFacade.wishesOf(loggedUser.id()));
        model.addAttribute("hasRecipient", recipientWishes.isPresent());
        recipientWishes.ifPresent(recipient -> model.addAttribute("recipientWithWishes", recipient));
    }

    private DtoUser loggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userFacade.findByLogin(auth.getName())
            .orElseThrow(RuntimeException::new);
    }

    @PostMapping("/lottery")
    public String startLottery() {
        if (lotteryFacade.annualLotteryNotPerformedYet()) {
            lotteryFacade.performLottery();
        }
        return "dashboard";
    }

}
