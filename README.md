# Venmo Ticket Dispenser

Andrew Lu

ECE 251, Spring 2020, Professor Isukapalli.

[View repository on GitHub](https://github.com/andrewhlu/venmo-ticket-dispenser)

---

## Overview

The goal of this project is to minimize the time spent waiting to purchase tickets at our cultural student organizations' annual night markets by automating digital wallet purchases using Android-based kiosks that can instantly detect and accurately dispense ticket purchases.

This idea was originally proposed for this year's TASA Night Market, which has unfortunately been cancelled due to the current situation. This idea is also based on a previous project in my freshman year where I created a web interface for using Venmo purchases to purchase items on e-commerce stores.

## Parts List

The following parts list reflects parts that will be used to build the application's accompanying ticket dispenser.

*This parts list is still a work in progress. Purchased quantities may be more than what is actually needed for the project.*

| Part | Quantity | Unit Price | Total Price |
|------|----------|------------|-------------|
| [Arduino Uno R3 Board](https://www.amazon.com/dp/B01EWOE0UU) | 1 | $12.99 | $12.99 |
| [(Pack of 4pcs) NEMA17 Stepper Motor High Torque Bipolar DC Step Motor Kit by MOTOU](https://www.amazon.com/dp/B07RZHWYQ9) | 1 | $33.88 | $33.88 |
| [BIQU A4988 Compatible StepStick Stepper Motor Diver Module with Heat Sink for 3D Printer Controller Ramps 1.4(Pack of 5pcs)](https://www.amazon.com/dp/B01FFGAKK8) | 1 | $8.99 | $8.99 |
| [HC-05 Wireless Bluetooth RF Transceiver](https://www.amazon.com/dp/B071YJG8DR) | 1 | $7.99 | $7.99 |
| [5V 50N Electromagnet](https://www.amazon.com/dp/B01N108S9A) | 1 | $9.39 | $9.39 |
| Photoresistor | 1 | ? | ? |

---

The original project proposal presentation can be found [here](proposal.pdf).

---

# Weekly Updates

## Week 6 Update (May 8)

This week was spent flushing out the project idea after an initial review by Professor Isukapalli. The project was presented near the end of class on Wednesday, May 6, and the project proposal slides can be found above.

The rest of this week was spent on getting a base Android application up and running. Since this app was intended to be used in a standalone kiosk environment, I spent a bit of time learning how to hide unnecessary components like the status bar and how to pin apps so they cannot be exited. Due to other projects and the BS/MS application, however, I was unable to spend any additional time working on the application.

## Week 7 Update (May 15)

I started this week by completing Lab 3. In doing so, I was able to learn how to make HTTP requests to endpoints, as well as continuously listen for for responses, both of which are essential components to my application. My project will be set up in a similar fashion to that of Lab 3, where my application will be interfacing with a backend server that will handle the task of scraping the owner's Gmail account and parsing any Venmo entries.

After completing Lab 3, I focused my efforts on ordering the necessary parts for the ticket dispenser. I decided to keep the ticket dispenser simple and simply dispense tickets using one or two stepper motors, with a photoresistor to detect pulses in light that result from ticket edges passing by. I also decided to add an electromagnet to secure tickets from forcefully being pulled out. The parts have been ordered as of May 17th, and should arrive within the next week.

Since the deadline for the BS/MS application was this week, I did not plan on working on the Android app this week. I will continue doing so next week, after my application has been submitted and I am able to catch up on my backlog of assignments.
