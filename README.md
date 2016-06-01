Chat
====
<img src="http://igorpi25.ru/screenshot/chat/banner_github.png" height="200" />

Это движок мессенжера, построенный с использованием open-source библиотек. Движок польностью модульный. Каждый модуль представляет отдельный open-source проект на Github, с описанинием и запускаемым демонстрационным приложением.

Демонстрационное приложение можете загрузить из GooglePlay

<img src="http://igorpi25.ru/screenshot/chat/google_play_icon.png" height="60" />

Архитектура
-----------

Мгновенная отзывчивость - это способность UI элементов на клиентском устройстве, мгновенно реагировать на изменения источников контента, расположенных на других клиентских устройствах, без необходимости запросов к серверу, и действий со стороны пользователя

Например, изменение фотографии профиля пользователя, отображается на устройствах всех пользователей мгновенно. Без необходимости перезагрузки фрагмента или активити. Изменение происходит во всех фрагментах, даже косвенно отображающих фотографию этого пользователя.

Другой пример. При получении нового сообщения, транспорт() записывает его в Sqlite БД. При записывании данных вызываетя ContentResolver, который через переданный ему URI вызывает `onContentChanged` в курсоре, который используется на UI фрагменте. В итоге, вызывается `onLoadFinished(Loader<Cursor> loader, Cursor data)` на нужном фрагменте. Таким образом UI-фрагменты мгновенно реагируют на изменения сервера. 

Способ описанный выше отличается простой реализацией, надежностью. Не требует `Intent` и `BraocastMessage` чтобы ловить изменение состояния системы. Методика применения MultipleTypesAdapter дополняет этото способ, и существенно упрощает разработку.

UI
--
Здесь и далее, применяется единая архитектура описанная выше. Прежде чем перейти к изучению кода, автор рекомендует ознакомиться с [MultipleTypesAdapter][81] и [Communicator][82]

##### ConversationPrivate.java

Чат с одним человеком. При изменении состояния Sqlite БД, вызывается `onLoadFinished(Loader<Cursor> loader, Cursor data)`. Он формирует новый `MatrixCursor` для адаптера. Адаптер "кушает" особым образом построееный курсор, который автор для простоты называет MatixCursor

Обратите внимание на `LOADER_USERS`, благодоря которым получается "мгновенная отзывчивость", на изменение профиля другого пользователя, без необходимости перезагрузить весь фрагмент. Перезагружается только нужный Loader.

<img src="http://igorpi25.ru/screenshot/chat/private_chat.png" height="200" />

##### ConversationGroup.java
Групповой чат, принцип работы одинаковый с чатом с одним человеком

<img src="http://igorpi25.ru/screenshot/chat/group_chat.png" height="200" />

##### Conversation.java
Это базовый класс для фрагментов `ConversationPrivate.java` и `ConversationGroup.java` отображающих непосредственно чат. Основную работу по отображению записей полученных из БД, делает именно этот класс

##### RecentList.java

Архитектура точно такая-же. Отличие данного UI фрагмента, в более сложном SQL-запросе, который расположен в `ContentProvider`. Т.е. на данном архитектурном уровне нет отличия от фрагментов описанных выше. Все тот же "мулти-адаптер" и "отзывчивость"

<img src="http://igorpi25.ru/screenshot/chat/recent_chats.png" height="200" />

UI элементы из Profile
----------------------
##### Contacts.java
<img src="http://igorpi25.ru/screenshot/chat/contacts_list.png" height="200" />

##### DetailsGroup.java
<img src="http://igorpi25.ru/screenshot/chat/group_profile.png" height="200" />
<img src="http://igorpi25.ru/screenshot/chat/group_profile_2.png" height="200" />

##### DetailsUser.java
<img src="http://igorpi25.ru/screenshot/chat/user_profile.png" height="200" />

Архитектура точно такая как предыдущие. Отличие данного UI фрагмента, в более сложном SQL-запросе, который расположен в `ContentProvider`. Т.е. на данном архитектурном уровне нет отличия от фрагментов описанных выше. Все тот же "мулти-адаптер" и "отзывчивость"

Серверная часть
---------------
**ВНИМАНИЕ!** Данная библиотека - это реализация только клиентской части. Серверную часть вы можете видеть в репозитории автора на [GitHub][4]. Есть инструкция по самостоятельному запуску и настройке сервера

Клонирование из GitHub
----------------------

Проект содержит много git-подмодулей, которые при неумелом использовании могут принести неприятности. Поэтому делайте как я, тогда с git-подмодулями проблем не будет

1. Для клонирования репозитория, автор рекомендует использовать командную строку, вместо EGit Eclipse или AndroidStudio. Т.к. проект содержит настройки workspace в папке репозитория, и репозитории подмодулей находятся НЕ в ветке **master**, и подмодули тоже имеют свои подмодули. В командной строке выполните:
	```
$ git clone git://github.com/Igorpi25/Chat.git Chat
$ cd Chat
$ git submodule update --remote --recursive --init
	```
Параметры третьей строки (`git submodule update --remote --recursive --init`) означают:
	* `--remote` - подмодуль нужно скачать из ветки удаленного репозитория. Название ветки записано из файле ".submodule" в параметре `branch`. Это ветка `library`, в ветке `master` находится запускаемый демо-проект
	* `--recursive` - повторяй команду `git submodule update --remote --recursive --init` для всех модулей, и их вложенных подмодулей
	* `--init` - если подмодуль не инициализирован, то инициализируй. (во вложенных подмодулях это очень кстати, без этого нам бы пришлось вызывать `git submodule init` для каждого вложенного подмодуля)

Используемые библиотеки
-----------------------
* [ActionBarSherlock][1]
* [Volley][2]
* [Glid][9] - используется в демо-проекте

Библиотеки автора
-----------------
* [Connection][84]- Диалоги ошибки соединения к интернету. Включен в составе Session
* [Session][8] - Авторизация пользователя на сервере
* [Uploader][83] - Отправка фотографий на сервер
* [Communicator][82] - это архитектурно важная библиотека. Все дела с Websocket, парсинг json и т.п.
* [MultipleTypesAdapter][81] - вся архитектура UI-элементов построена на этой библиотеке
* [Profile][85] - профили пользователся и группы. Модуль обеспечивает элементы "социальной сети"

License
-------

See the [LICENSE](LICENSE) file for license rights and limitations (Apache).

[1]: http://actionbarsherlock.com/
[2]: https://github.com/mcxiaoke/android-volley
[4]: https://github.com/Igorpi25/server_v2
[5]: https://git-scm.com/book/en/v2/Git-Tools-Submodules/
[6]: https://github.com/Igorpi25/Profile
[7]: http://www.androidhive.info/2014/01/how-to-create-rest-api-for-android-app-using-php-slim-and-mysql-day-12-2/

[8]: https://github.com/Igorpi25/Session
[81]: https://github.com/Igorpi25/MultipleTypesAdapter
[82]: https://github.com/Igorpi25/Communicator
[83]: https://github.com/Igorpi25/Uploader
[84]: https://github.com/Igorpi25/Connection
[85]: https://github.com/Igorpi25/Profile
[9]: https://github.com/bumptech/glide
[10]:https://code.google.com/archive/p/httpclientandroidlib/
