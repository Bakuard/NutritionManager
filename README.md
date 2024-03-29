# NutritionManager

---

## Содержание
[Назначение](#goals)\
[Как использовать](#howToUse)\
[Ссылки](#links)

---

<a name="goals"></a>
## Назначение
Приложение для автоматического расчета денежных затрат на закупку продуктов для любого блюда.
Предназначено для людей регулярно занимающихся планированием и готовкой пищи.\
Для выбранного пользователем блюда (или сразу нескольких блюд) и кол-ва порций приложение составит
список продуктов, которые надо купить, рассортирует их по магазинам, рассчитает их
общую стоимость и для каждого продукта в списке укажет:
* Необходимое кол-во для заданного числа порций блюда
* Недостающее кол-во для заданного числа порций блюда (приложение позволяет вести учет продуктов 
уже имеющихся в наличии у пользователя, и при составлении списка учитывает их)
* Суммарную стоимость недостающего кол-ва этого продукта для заданного числа порций блюда
* Наименование
* Сорт или разновидность
* Производителя или марку
* Фасовку (кол-во данного продукта в одной упаковке)

---

<a name="howToUse"></a>
## Как использовать
Исходные данные для составления списка закупаемых продуктов разбиты на три раздела: продукты, блюда и меню.

В разделе "продукты" пользователь ведет список продуктов общий для всех его блюд. Продукты различаются сочитанием
полей "производитель", "категория", "сорт", "магазин" и "размер упаковки".

В разделе "блюда" пользователь ведет список всех своих блюд и для каждого блюда указывает его состав на одну порцию.

В разделе "меню" пользователь может объединять блюда в группы указывая кол-во порций каждого блюда в этой группе.
В приложении NutritionManager такие группы называются "меню".

Сценарий получения списка закупаемых продуктов:
1. Пользователь - указывает блюдо или меню(в качестве группы блюд), и делает запрос на составления списка.
2. Система - для каждого игредиента блюда указывает перечень подходящих продуктов и предлагает пользователю
уточнить какой именно продукт ему нужен. По умолчанию выбирает самые дешевые.
3. Пользователь, при необходимости, уточняет продукт для выбранных им ингредиентов блюда (этот шаг можно пропустить).
4. Система возвращает список продуктов в виде PDF файла.

---

<a name="links"></a>
## Ссылки
[Перейти к приложению NutritionManager](https://nutrition-dev.herokuapp.com/)\
[Документация endpoints (для разработчиков)](https://nutritionmanager.xyz/api)