package com.autoalert.autoalert.Controller;

import com.autoalert.autoalert.Model.Dto.ResetPasswordDto;
import com.autoalert.autoalert.Model.Dto.UserCreateDto;
import com.autoalert.autoalert.Model.User;
import com.autoalert.autoalert.Service.EmailService;
import com.autoalert.autoalert.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/register/form")
    public String displayUserForm(Model model) {
        model.addAttribute("userCreateDto", new UserCreateDto());
        return "createUser";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid UserCreateDto userCreateDto,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userCreateDto", userCreateDto);
            return "createUser";
        }
        if (userService.findByEmail(userCreateDto.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.email", "Emailul tau a fost deja inregistrat!");
            return "createUser";
        }
        if (!userCreateDto.getPassword().equals(userCreateDto.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "error.verifyPassword", "Parolele nu se potrivesc!");
            return "createUser";
        }

        String verificationCode = userService.generateVerificationCode();

        userCreateDto.setVerificationCode(verificationCode);
        User user = userCreateDto.mapToUser();

        userService.createUser(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        model.addAttribute("email", user.getEmail());
        return "verifyEmail";
    }
    @PostMapping("/verifyCode")
    public String verifyCode(@RequestParam String email,
                             @RequestParam String verificationCode,
                             Model model) {

        Optional<User> optionalUser = userService.findByEmail(email);

        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Email invalid.");
            return "verifyEmail";
        }

        User user = optionalUser.get();

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(verificationCode)) {
            user.setActive(true);
            user.setVerificationCode(null);
            userService.updateUser(user);

            return "verifyCodeSuccess";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("error", "Codul introdus nu este valid!");
            return "verifyEmail";
        }
    }
    @GetMapping("/loginForm")
    public String showLoginPage() {
        return "loginForm";
    }

    @GetMapping("/resetPasswordForm")
    public String resetPasswordForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("resetPasswordDto", new ResetPasswordDto());
        model.addAttribute("email", email);
        return "resetPassword";
    }


    @PostMapping("/resetPassword")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordDto resetPasswordDto,
                                BindingResult result,
                                @RequestParam("email") String email,
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("resetPasswordDto", resetPasswordDto);
            return "resetPassword";
        }
        if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.resetPasswordDto", "Parola și confirmarea parolei nu se potrivesc.");
            return "resetPassword";
        }
        try {
            userService.resetPassword(email, resetPasswordDto.getNewPassword());
            emailService.sendPasswordChangedConfirmationEmail(email);
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.resetPasswordDto", e.getMessage());
            return "resetPassword";
        }
        return "redirect:/passwordChangedSuccess";
    }
    @GetMapping("/forgotPassword")
    public String showForgotPasswordPage() {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Utilizatorul cu acest email nu a fost găsit.");
            return "forgotPassword";
        }

        String resetLink = "http://localhost:8080/resetPasswordForm?email=" + email;

        try {
            emailService.sendResetPasswordEmail(email, resetLink);
            model.addAttribute("email", email);
            return "emailSentView";
        } catch (Exception e) {
            model.addAttribute("error", "A apărut o eroare la trimiterea emailului.");
            return "forgotPassword";
        }
    }

    @GetMapping("/passwordChangedSuccess")
    public String successPassword() {
        return "resetPasswordSuccess";
    }

    @GetMapping("/upgrade")
    public String showUpgradePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        boolean isPremium = userService.isUserPremium(email);
        model.addAttribute("isPremium", isPremium);
        return "upgrade";
    }

    @PostMapping("/upgrade")
    public String doUpgrade(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.upgradeUserToPremium(email);
        return "redirect:/dashboard?upgradeSuccess";
    }
}
