### JPipeline
*English version will be available later*  

JPipeline - это аналог [Node-RED](https://github.com/node-red/node-red), написанный на Java.  
Представляет собой систему (сервер + клиент), предоставляющую возможность 
программирования потоков данных в графическом режиме.

<img src="../assets/images/main_menu.png" alt="Main menu" width="600"/>

## Содержание
1. [Потоковое программирование](#flow-based-programming)
2. [Модули](#modules)
3. [Процесс работы](#work-process)
4. [Реализованные узлы](#nodes)
5. [Запуск](#run)
6. [Разработка пользовательских узлов](#custom-nodes)

## Потоковое программирование <a name="flow-based-programming"></a>
Потоковое программирование (Flow-based programming, FBP) – это парадигма программирования, которая 
определяет приложения как сети процессов, представляющих собой 
«черные ящики» (узлы), которые обмениваются данными по предопределенным 
соединениям (дугам) посредством передачи сообщений. Узлы могут иметь входы и выходы. 
Принимая сообщение на выход, узел может произвести некоторые вычисления и 
отправить новое сообщение на выход. Эти узлы можно беско-нечно связывать 
между собой для формирования различных приложений без необходимости внутреннего изменения. 

## Модули <a name="modules"></a>
1. jpipeline-executor (`Исполнитель`) - серверное приложение, выполняющее потоки
2. jpipeline-manager (`Менеджер`) - серверное приложение, управляющее исполнителем
3. jpipeline-javafx-client (`Клиент`) - настольное приложение с пользовательским интерфейсом

| ![Диаграмма развертывания](../assets/images/deployment_diagram.png?raw=true) | 
|:--:| 
| *Диаграмма развертывания* |

## Процесс работы <a name="work-process"></a>
### Экран входа <a name="login-screen"></a>
При запуске клиентского приложения открывается экран входа – здесь можно ввести данные для подключения 
к серверной части, при успехе данные сохраняются на диск для упрощения последующих подключений.

| ![Экран входа](../assets/images/login_menu.png?raw=true) | 
|:--:| 
| *Экран входа* |

### Основной экран <a name="main-screen"></a>
После успешного входа мы сразу попадаем на главный экран приложения.   
В центральной его части отображается рабочая область с расположенными на ней узлами – 
это графическое представление конфигурации, которую мы получили с сервера.
Узлы можно перемещать, удалять, соединять между собой и редактировать их свойства, пример меню редактирования будет представлен далее.  
В левой части расположено меню со списком доступных типов узлов. При нажатии на один из них он попадает на рабочую область.  
* удаление узла - клик `ПКМ` по узлу
* удаление связи - клик `ПКМ` по связи
* редактирование узла - двойной клик `ЛКМ` по узлу

В нижней части экрана находится панель инструментов, которая содержит кнопки для управления и статусы серверных приложений:
1. Кнопка `Deploy` - отправляет текущую конфигурацию потока на сервер и перезапускает поток
2. Кнопка `Reset` - сбрасывает текущую конфигурацию до той, что запущена на сервере в данный момент
3. Кнопка `Debug` - открывает меню отладки
4. Кнопка `Login` - открывает меню входа

| ![Основной экран](../assets/images/main_menu.png?raw=true) | 
|:--:| 
| *Основной экран* |

### Меню редактирования <a name="edit-menu"></a>
При двойном клике `ЛКМ` по узлу на рабочей области открывается меню редактирования.  
Разработчик узла пишет меню редактирования сам, есть несколько способов:
1. Упрощенный `FXML`, поддерживаются только текстовые поля и выпадающие списки (пример - [ SplitNode](jpipeline-standart-nodes/src/main/resources/node-resources/SplitNode))
2. `FXML` + `Groovy` (deprecated) - произвольная разметка с помощью FXML и контроллер, написанный на Groovy (пример - [SwitchNode](jpipeline-standart-nodes/src/main/resources/node-resources/SwitchNode))
3. `HTML` + `JavaScript` (WebView) - используется HTML-файл, содержащий всю логику по отрисовке и обработке меню (пример - [SwitchNode](jpipeline-standart-nodes/src/main/resources/node-resources/SwitchNode))

| ![Меню редактирования SqlSelectNode](../assets/images/edit_menu_example.png?raw=true) | 
|:--:| 
| *Меню редактирования SqlSelectNode* |

| ![Меню редактирования SplitNode](../assets/images/edit_menu_example_2.png?raw=true) | 
|:--:| 
| *Меню редактирования SplitNode* |

### Меню отладки <a name="debug-menu"></a>

По-умолчанию любое сообщение, приходящее на вход узлу `DebugNode`, будет отправлено в меню отладки.
Помимо этого, в классе [`Node`](jpipeline-common/src/main/java/com/jpipeline/common/entity/Node.java) 
в качестве логгера используется экземпляр класса [`JLogger`](jpipeline-common/src/main/java/com/jpipeline/common/util/JLogger.java), 
поэтому при использовании переменной `log` в вашем узле все вызовы `log.error()` и `log.debug() `
будут выводить сообщение так же и в меню отладки в пользовательском интерфейсе.

| ![Меню отладки](../assets/images/debug_menu.png?raw=true) | 
|:--:| 
| *Меню отладки* |

## Реализованные узлы <a name="nodes"></a>
### InjectNode
Узел данного типа позволяет отправить сообщение, указанное в настройках, 
по нажатию специальной кнопки в интерфейсе пользователя.
| <img src="../assets/images/inject_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню InjectNode* |
### DelayNode
Позволяет искусственно замедлить поток, добавляя задержку для каждого сообщения относительно предыдущего. 
| ![Меню DelayNode](../assets/images/delay_node_menu.png?raw=true) | 
|:--:| 
| *Меню DelayNode* |
### FunctionNode
Позволяет написать функцию на Groovy, которая будет применяться к кажому входящему сообщению, 
результат будет отправлен следующему узлу. Таким образом, к примеру, 
можно перемещать свойства сообщения, удалять их или устанавливать новые.
| <img src="../assets/images/function_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню FunctionNode* |
### SwitchNode
Аналог оператора “switch”, доступного в большинстве языков программирования –
пользователь может указать определенные условия, по которым 
сообщение будет направляться на разные выходы узла.
| <img src="../assets/images/switch_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню SwitchNode* |
### JsonNode
Выполняет преобразование json-строк в объекты и наоборот.
| <img src="../assets/images/json_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню JsonNode* |
### SplitNode
Позволяет разделить входящее сообщение на несколько исходящих.  
Умеет:
* резделять строки с помощью указанного разделителя
* разделять массивы на подмассивы с указанными размером 
* разделять объект на несколько сообщений на основе его свойств

| <img src="../assets/images/split_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню SplitNode* |
### TemplateNode
Позволяет генерировать сообщения на основе текстового шаблона, используется синтаксис Mustache.
| <img src="../assets/images/template_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню TemplateNode* |
### ExecNode
Позволяет выполнить консольную команду, подстановка аргументов по Mustache-шаблону. 
Вывод команды отправляет в виде сообщения следующему узлу. 
| <img src="../assets/images/exec_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню ExecNode* |
### CounterNode
Каждое сообщение, проходящее через такой узел, увеличивает его внутренний 
счетчик на единицу. Значение счетчика пользователь видит в 
статусе этого узла. Имеет кнопку, нажатие которой сбрасывает счетчик. 
### SqlSelectNode
Позволяет выполнить SELECT-запрос к базе данных. 
Нет ограничений на типы баз данных, но необходимо загрузить 
jdbc-драйвер для нужной СУБД в папку с расширениями. 
На вход принимает сообщение, подставляя его поля в запрос. 
Имеет два режима работы:
* выборка целиком отправляется в одном сообщении
* одна запись – одно сообщение

| <img src="../assets/images/sql_select_node_menu.png?raw=true" width="500"/> | 
|:--:| 
| *Меню SqlSelectNode* |

## Запуск <a name="run"></a>

### Серверная часть <a name="server-side"></a>
```bash
docker run -p 9543:9543 -p 7000:7000 -v ~/.jpipeline/libs:/home/jpipeline/libs iamhook/jpipeline
```
### Клиентская часть <a name="client-side"></a>
```bash
java -jar jpipeline-javafx-client.jar
```

## Разработка пользовательских узлов <a name="custom-nodes"></a>
Разработчикам доступна возможность создавать собственные узлы. 
Для этого необходимо поместить jar-файл с узлами в папку с расширениями на сервере (libs).

### Шаблон <a name="template"></a>
Склонируйте проект [jpipeline-custom-nodes-template](https://github.com/iamhook/jpipeline-custom-nodes-template).
Этот проект содержит реализацию узла `HelloWorldNode`, на основе которой можно начать разработку своих узлов.

После разработки необходимо выполнить `./gradlew jar`. В папке `build/libs` появится jar-файл, 
который необходимо положить в папку с расширениями на сервере (при использовании для запуска команды выше это ~/.jpipeline/libs на хостовой системе).

### Конфигурация типа узла <a name="node-type-configuration"></a>

Конфигурация типа узла представляет собой json-файл, имеющий следующие пары ключ-значение:
* category - категория узла, используется для отображения типа узла в палитре
* color - цвет узла
* editMode - тип реализации меню (NONE, SIMPLE, HTML_JAVASCRIPT, FXML_GROOVY)
* inputs - количество входов, может принимать значения от 0 до 1
* outputs - количество выходов, может принимать значения от 0 до бесконечности
* properties - свойства узла

Конфигурация свойств, в свою очередь, может иметь следующие ключи:
* name - имя свойства
* type - тип свойства (NUMBER, STRING, BOOLEAN, COMPLEX)
* hasButton - наличие кнопки (false, true)
* defaultValue - значение по-умолчанию
* variants - возможные значения в формате "значение:полное имя"
* nested - вложенные свойства (имеют ту же структуру, гипотетически доступна бесконечная вложенность)
* required - флаг обязательности (в данный момент не используется)
* multiple - флаг множественности

#### Пример конфигурации
```javascript
{
  "category": "Function",
  "color": "#999999",
  "editMode": "HTML_JAVASCRIPT",
  "inputs": 1,
  "outputs": 2,
  "properties": [
    {
      "name": "property",
      "type": "STRING",
      "defaultValue": "payload",
      "required": true
    },
    {
      "name": "condition",
      "type": "COMPLEX",
      "defaultValue": [
        {
          "operator": "==",
          "value": 1
        },
        {
          "operator": "==",
          "value": 2
        }
      ],
      "nested": [
        {
          "name": "operator",
          "type": "STRING",
          "variants": {
            "==": "==", "!=": "!="
          },
          "defaultValue": "==",
          "required": true
        },
        {
          "name": "value",
          "type": "STRING",
          "defaultValue": null,
          "required": true
        }
      ],
      "required": true,
      "multiple": true
    }
  ]
}
```

### Программный код узла <a name="node-program-code"></a>

При создании узла необходимо унаследоваться от класса `com.jpipeline.common.entity.Node` (модуль jpipeline-common).  
Также нужно переопределить методы `onInit()` и `onInput(JPMessage message)`.  
При наличии у узла кнопки нужно переопределить метод `pressButton()`.  

Для отправки сообщения используется метод `send(JPMessage message)` или `send(JPMessage message, int output)`.  
Для установки статуса используется метод `setStatus(NodeStatus status)`.  
Для логирования можно использовать `log` - член класса `Node`, который отправляет все debug и error в меню отладке в клиентском приложении.  

Также можно переопределить метод `subscribe(Node subscriber)` или  `subscribe(Node subscriber, int output)`.  
С помощью него  можно получить доступ к flux. Это используется, например, в `DelayNode`.  

Каждое свойство узла должно помечаться аннотацией `@NodeProperty`.  
Если свойство множественное, оно должно иметь тип `java.util.List`.  
Для комплексного (type=COMPLEX) свойства необходимо создать класс, описывающий это свойство (пример ниже, класс свойства `Condition`).

#### Пример

```java
package com.jpipeline.entity.function;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class SwitchNode extends Node {

    @NodeProperty
    private List<Condition> condition;

    @NodeProperty
    private String property;

    public SwitchNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {

        int i = 0;
        for (Condition cond : condition) {
            String operator = cond.getOperator();
            String value = cond.getValue();
            if (operator.equals("==") && message.get(property).equals(value)) {
                send(message, i);
            }
            if (operator.equals("!=") && !message.get(property).equals(value)) {
                send(message, i);
            }

            i++;
        }
    }

    @Getter @Setter @NoArgsConstructor
    private static class Condition {
        private String operator;
        private String value;
    }

}
```

