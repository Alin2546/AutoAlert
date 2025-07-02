package com.autoalert.autoalert.Service;

import com.autoalert.autoalert.Model.CarDocument;
import com.autoalert.autoalert.Service.CarDocumentService;
import com.autoalert.autoalert.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CarDocumentService carDocumentService;
    private final EmailService emailService;


    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryNotifications() {
        sendNotificationsForDaysBeforeExpiry(7);
        sendNotificationsForDaysBeforeExpiry(3);
    }

    private void sendNotificationsForDaysBeforeExpiry(int daysBefore) {
        LocalDate targetDate = LocalDate.now().plusDays(daysBefore);
        List<CarDocument> expiringDocs = carDocumentService.findDocumentsByExpiryDate(targetDate);

        for (CarDocument doc : expiringDocs) {
            String userEmail = doc.getCar().getUser().getEmail();
            String subject = "Reminder: Document expiră în curând";
            String message = String.format(
                    "Documentul %s pentru mașina %s %s expiră peste %d zile, pe data %s.",
                    doc.getDocumentType(),
                    doc.getCar().getBrand(),
                    doc.getCar().getModel(),
                    daysBefore,
                    doc.getExpiryDate()
            );
            emailService.sendExpiryNotificationEmail(
                    userEmail,
                    doc.getDocumentType().toString(),
                    doc.getCar().getBrand(),
                    doc.getCar().getModel(),
                    doc.getExpiryDate(),
                    daysBefore
            );
        }
    }
}
