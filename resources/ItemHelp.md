---
command: "2108"
name: "Pre Access Code"
---
You may program a Pre Access Code which will then be required to be prepended to all commands EXCEPT UNLOCK CODES AND COMMANDS ENTERED WHILE THE RC210 IS LOCKED. In other words, you will need to use any programmed Pre Access Code before any Command Macro. This is not to be confused with the Pre Command Prefix above.

Probably the most common use of a Pre Access Code is as a Site Prefix in a multi-controller, linked system. By using such a prefix, all controllers in the system can use identical codes with the Pre Access Code serving as a site address. For example, let’s say we have a linked system consisting of 3 repeaters, all linked together full-time. At each site, we use the command of “ABC” to read backup battery voltage. So we assign site #1 a Pre Access Code of “1”, site #2 to “2” and site #3 to “3”. Now we access site #3 from site #1 and send DTMF digits 3 A B C. Site #3 will now read back its backup battery voltage down the link back to site #1. Similarly, we can use addresses in this manner for and from anywhere within our system.

*2108x where x is 1 to 3 digits. If programmed to 0, no Pre Access Code is used. If programmed to 0, no Pre Access Code is used.