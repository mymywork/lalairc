		Идентификаторы.  
  
1. комманда $txt 		- вызывает эдитбокс для ввода.  
2. комманда $txt(Hello world)	- вызывает эдитбокс для ввода с уже вставленными текстом Hello world.  
3. комманда $itxt 		- вызывает едитбокс со вcтавленным ником.  
4. комманда $nick 		- встваляет в праметры комманды ник выбранный из никлиста канала.(также может вставлять названия итемов из листа.)  
5. комманда $chan		- вставляет название текушего канала на котором вы находитесь.  
6. комманда $etopic		- вызывает эдитбокс редактирования топика с конвертирование контрольных тегов.  
7. комманда $eexbuf		- буффер обмена.(copypaste)  
8. комманда $nitem		- номер итема выбранного из меню  
  
		Комманды.  
  
raw (text)			- посылает рав комманду ирцсерверу.  
list (param)			- вызывает лист каналов с парметром или без.  
updatebuf (text)		- вставляет в буффер обмена текст.  
quit (text)			- quit с мессаджем или без.  
whois (nick)			- whois ник.  
msg (chan/nick) (text)		- отправляет мессадж  
notice (chan/nick) (text)	- отправляет нотис.  
join (chan)			- заход на канал.  
rejoin (chan)			- перезаходит на канал.  
topic (chan) (text)		- устанавливает топик на канале.  
ctcp (chan/nick) (ctcp-req)	- посылает ctcp запрос.  
nicklist			- вызывает меню никлиста.  
banlist (chan)			- вызывает банлист канала.  
explist (chan)			- вызывает експешион лист канала.  
invlist (chan)			- вызывает инвайт лист канала.  
mode (chan/nick) (mode)		- устанавлиает режим.  
part (chan)			- уйти с канала.  
query (nick)			- открывает окошко привата.  
me (chan/nick) (text)		- делает экшен.  
away (text)			- устанвлиает сообщение серверного авея.  
invite (nick) (chan)            - пригласить ник на канал.  
ban (chan) (nick) (type)        - забанить на канале ник по типу бана.  
kick (chan) (nick) (text)       - кикнуть с канал ник с мессаджем.  
kickban (chan) (nick) (type) (text) - забанить по типу и кикнуть с канала ник.  
winlist				- вызывает меню списка окон.  
showwin	(status/chan/nick)	- показать окно.  
copytobuf (0/1/2/3)		- копирует выделеный маркер консоли в буффер.  
					  0 - копирует с цветами.  
					  1 - копирует без цветов.  
					  2 - добавляет с цветами к буфферу.  
					  3 - добавляет без цветов к буфферу.  
close (chan/nick)			- закрывает окно.  
addmenucmd (menu) (name:cmd)	- добавляет в меню комманду.  
addmenu (menu) (name)		- дабавляет подменю.  
delmenu (menu)			- удаляет меню.  
editmenu (menu)			- редактирует меню.  
upmenu (menu)			- перемещает пунк меню выше на один.  
downmenu (menu)			- перемещает пунк меню ниже на один.  
