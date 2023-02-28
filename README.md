# rc210-explorer
View an RC-210 RCP file for humans.

This is a web-based relacvement for the Arcom RCP program. 
It was inspired by https://github.com/KenArck/WebRCP written in PHP. I don't know [PHP](https://www.quora.com/Is-PHP-the-worst-programming-language-to-learn) and i'm too old to learn it.

But I do know [Scala](https://www.scala-lang.org) a modern functional programming language. So this is written Scala.

# Features
* Numbered items can haven names.
  * Schedules
  * Macros
  * Message Macros
- Designed for Human repeater operators. For example a Schedule show the conection to a named maco, which shows the name of the functions of the macro and additional info like the text of a Message Macro.
-- Show relationships between the varous part of the configuration. F
- Run on the JVM, run anywhere. But I'll proably only test on the Mac, Linux and maybe Microsoft Windows 11
- Data is persisted in JSON text files. No database required.
- Use the [Play Framework](https://www.playframework.com) 


./rc210-explorer -Dplay.http.secret.key='QCY?tAnfIlR6CTf:G3gk?aZ?iwrNwnxf:90Latabg@5241AB`R5W:1uDFN];Ik@n
