package com.devtiro.realestate.services;

import com.devtiro.realestate.domain.entities.PropertyViewing;
import com.devtiro.realestate.domain.entities.Role;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");


    @Async
    public void notifyAgentOnViewingRequest(String agentEmail, PropertyViewing viewing) {
        try {
            var message = new SimpleMailMessage();
            message.setTo(agentEmail);
            message.setSubject("New Property Viewing Request");
            message.setText(String.format(
                    "You have received a new viewing request:\n\n" +
                            "Property: %s\n" +
                            "Address: %s\n" +
                            "Requested By: %s\n" +
                            "Contact: %s\n" +
                            "Phone: %s\n" +
                            "Scheduled Time: %s\n\n" +
                            "Please confirm or reschedule the viewing.",
                    viewing.getPropertyTitle(),
                    viewing.getPropertyAddress(),
                    viewing.getUserName(),
                    viewing.getUserEmail(),
                    viewing.getUserPhone(),
                    viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER)
            ));

            mailSender.send(message);
            log.info("Sent viewing request notification to agent: {}", agentEmail);
        } catch (Exception e) {
            log.error("Failed to send viewing request notification to agent: {}", agentEmail, e);
        }
    }

    @Async
    public void notifyUserOnViewingRequest(String userEmail, PropertyViewing viewing) {
        try {
            var message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Property Viewing Request Received");
            message.setText(String.format(
                    "Your viewing request has been received:\n\n" +
                            "Property: %s\n" +
                            "Address: %s\n" +
                            "Scheduled Time: %s\n\n" +
                            "The agent will confirm your viewing shortly. You will receive another email once confirmed.",
                    viewing.getPropertyTitle(),
                    viewing.getPropertyAddress(),
                    viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER)
            ));

            mailSender.send(message);
            log.info("Sent viewing confirmation to user: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send viewing confirmation to user: {}", userEmail, e);
        }
    }

    @Async
    public void notifyUserOnConfirmedViewingByAgent(PropertyViewing viewing, User agent) {
        try {
            String agentName = agent.getFirstName() + " " + agent.getLastName();
            var message = new SimpleMailMessage();
            message.setTo(viewing.getUserEmail());
            message.setSubject("Property Viewing Confirmed");
            message.setText(String.format(
                    "Your viewing request has been confirmed:\n\n" +
                            "Property: %s\n" +
                            "Address: %s\n" +
                            "Confirmed By: %s\n" +
                            "Contact: %s\n" +
                            "Phone: %s\n" +
                            "Scheduled Time: %s\n\n" +
                            "The agent has confirmed your viewing! In any problems please find the contact email and phone number above.",
                    viewing.getPropertyTitle(),
                    viewing.getPropertyAddress(),
                    agentName,
                    agent.getEmail(),
                    agent.getPhoneNumber(),
                    viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER)
            ));

            mailSender.send(message);
            log.info("Sent viewing confirmation to user: {}", viewing.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to send viewing confirmation to user: {}", viewing.getUserEmail(), e);
        }
    }

    @Async
    public void notifyViewingRescheduled(PropertyViewing viewing, User userPrincipal) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject("Property Viewing Rescheduled");
            if (userPrincipal.getRole().equals(Role.AGENT)) {
                message.setTo(viewing.getUserEmail());
                message.setText(String.format(
                        "Your property viewing has been rescheduled:\n\n" +
                                "Property: %s\n" +
                                "Address: %s\n" +
                                "New Time: %s\n\n" +
                                "If you have any questions, please contact your agent.",
                        viewing.getPropertyTitle(),
                        viewing.getPropertyAddress(),
                        viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER)
                ));
                mailSender.send(message);
                log.info("Sent viewing rescheduled notification to user: {}", viewing.getUserEmail());
            } else if(userPrincipal.getRole().equals(Role.USER)) {
                message.setTo(viewing.getAgentEmail());
                message.setText(String.format(
                        "Your property viewing has been rescheduled:\n\n" +
                                "Property: %s\n" +
                                "Address: %s\n" +
                                "New Time: %s\n\n" +
                                "If you have any problems, please contact the client.",
                        viewing.getPropertyTitle(),
                        viewing.getPropertyAddress(),
                        viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER)
                ));
                mailSender.send(message);
                log.info("Sent viewing rescheduled notification to agent: {}", viewing.getAgentEmail());
            } else {
                throw new UnauthorizedException("Invalid user role");
            }

        } catch (Exception e) {
            log.error("Failed to send rescheduled notification", e);
        }
    }


    @Async
    public void notifyViewingCancelled(PropertyViewing viewing, User userPrincipal) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject("Property Viewing Cancelled");
            if (userPrincipal.getRole().equals(Role.AGENT)) {
                message.setTo(viewing.getUserEmail());
                message.setText(String.format(
                        "Your property viewing has been cancelled:\n\n" +
                                "Property: %s\n" +
                                "Address: %s\n" +
                                "Original Time: %s\n" +
                                "Reason: %s\n\n" +
                                "Please contact the agent if you'd like to schedule a new viewing.",
                        viewing.getPropertyTitle(),
                        viewing.getPropertyAddress(),
                        viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER),
                        viewing.getCancellationReason()
                ));
                mailSender.send(message);
                log.info("Sent viewing cancelled notification to user: {}", viewing.getUserEmail());
            } else if(userPrincipal.getRole().equals(Role.USER)) {
                message.setTo(viewing.getAgentEmail());
                message.setText(String.format(
                        "Your property viewing has been cancelled:\n\n" +
                                "Property: %s\n" +
                                "Address: %s\n" +
                                "Original Time: %s\n" +
                                "Reason: %s\n\n" +
                                "Please contact the client if you'd like to schedule a new viewing.",
                        viewing.getPropertyTitle(),
                        viewing.getPropertyAddress(),
                        viewing.getScheduledDateTime().format(DATE_TIME_FORMATTER),
                        viewing.getCancellationReason()
                ));
                mailSender.send(message);
                log.info("Sent viewing cancelled notification to agent: {}", viewing.getAgentEmail());
            } else {
                throw new UnauthorizedException("Invalid user role");
            }

        } catch (Exception e) {
            log.error("Failed to send rescheduled notification", e);
        }
    }
}
