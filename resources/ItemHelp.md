---
command: "2108"
name: "Pre Access Code"
---
You may program a Pre Access Code which will then be required to be prepended to all commands EXCEPT UNLOCK CODES AND COMMANDS ENTERED WHILE THE RC210 IS LOCKED. In other words, you will need to use any programmed Pre Access Code before any Command Macro. This is not to be confused with the Pre Command Prefix above.

Probably the most common use of a Pre Access Code is as a Site Prefix in a multi-controller, linked system. By using such a prefix, all controllers in the system can use identical codes with the Pre Access Code serving as a site address. For example, let’s say we have a linked system consisting of 3 repeaters, all linked together full-time. At each site, we use the command of “ABC” to read backup battery voltage. So we assign site #1 a Pre Access Code of “1”, site #2 to “2” and site #3 to “3”. Now we access site #3 from site #1 and send DTMF digits 3 A B C. Site #3 will now read back its backup battery voltage down the link back to site #1. Similarly, we can use addresses in this manner for and from anywhere within our system.

*2108x where x is 1 to 3 digits. If programmed to 0, no Pre Access Code is used. If programmed to 0, no Pre Access Code is used.

---
command: "2109"
name: "Pre Command Prefix"
---
Sometimes it may desirable to be able to access commands that haven’t been remapped using a Command Macro without having to first unlock a Port. By defining a Pre Command Prefix (up to 4 digits is permissible), you can access those commands that normally require unlocking a Port with the exception of programming commands that start with a star ( *). Any command shown in Appendix C is accessible in this manner. You may also disable this feature if it’s not needed by programming a zero:

*2109x where x is from 1 to 4 digits for use as. If programmed to 0, no Prefix is used and this feature is disabled. 

If disabled you must first unlock a Port in order to access commands.

---
command: 1000
name: Hang Time
---
Each port has 3 unique programmable hang timers when that port is configured for full-duplex operation (in half-duplex, there is no hang time). It is programmed in 1/10 second intervals. For example, 1 second would be programmed with a value of "10".
*1000yx where “y” is the Hang Timer 1, 2 or 3 and "x" is the amount of hang time for that Hang Timer in 1/10 seconds. The range is 0 to 25.5 seconds. The controller responds with "H A N G TIMER <1,2 or 3> SET". Note: setting a timer to 0 gives zero hangtime
