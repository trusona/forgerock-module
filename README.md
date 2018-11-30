# ForgeRock AM Authentication Module

## Supported Versions

The Trusona AM Authentication module is developed against and supported on version 5.5 of ForgeRock AM. For newer versions of ForgeRock AM, Trusona also has a [ForgeRock AM Authentication Node](https://github.com/trusona/forgerock-node).


## Installation

To install the Trusona Forgerock Module, download the latest `trusona-forgerock-module-x.x.x-all.jar` from the [releases page](https://github.com/trusona/forgerock-module/releases), copy the jar into `WEB-INF/lib/` where AM is deployed, and then restart AM or the container in which it runs. See [Building and Installing the Sample Auth Module](https://backstage.forgerock.com/docs/am/5.5/authentication-guide/#build-config-sample-auth-module) for more details on installing custom authentication modules.


## Configuration

Before configuring the module, make sure you have your Trusona API token and secret. You will need to provide these values to the module so it can create Trusonafications. For steps on how to add a new Authentication module, see the [ForgeRock AM docs](https://backstage.forgerock.com/docs/am/5.5/authentication-guide/#build-config-sample-auth-module). When you are adding the Trusona Authentication Module, you will need to provide the following configuration values:

1. Action - A string that will be used in the action field of a Trusonafication. See the following section for more details.
1. Resource - string that will be used in the resoure field of a Trusonafication. See the following section for more details.
1. Trusona API Token - The API token you received from Trusona. This will be used to authenticate your module to Trusona's backend services.
1. Trusona API Secret - The API secret you received from Trusona. This will be used to authenticate your module to Trusona's backend services.
1. Deeplink URL - The URL that the user will be redirected to when they are on a mobile browser. This URL should be handled by the mobile app users will authenticate with. If not set, the user will be sent to the Trusona App.


### The Action and Resource fields

When you attempt to authenticate a user with this module, a Trusonafication will be issued for the user. The Accept/Reject screen for the Trusonafication will use the action and resource to display a sentence in the format "$customer_name would like to confirm your $action to $resource". So if you configure Action to be "login", and your Resource to be "ForgeRock", the sentence will read "$customer_name would like to confirm your login to ForgeRock"


### Mapping Trusona Users to ForgeRock Subjects

To map your Trusona account to your ForgeRock profile, you'll need to configure your authentication settings to map your Trusona identifier to a ForgeRock identifier. To do this, under your realm, go to `Authentication` -> `Settings` -> `User Profile` and edit the `Alias Search Attribute Name` to include the LDAP attribute that contains your Trusona identifier. The Trusona identifier may be different depending on how you are using Trusona (Trusona App vs Trusona SDK).


#### Mapping Users Registered with the Trusona App

The Trusona App uses verified email addresses as the user's identifiers. To map a Trusona App user, you can enter the `mail` LDAP attribute and it will look for ForgeRock profiles that contain a matching `Email Address`. See the screenshot below:

![Using email to map users](./images/search-alias-by-mail.jpg)


#### Mapping Users Registered with the Trusona SDK

If you are using the Trusona SDK within your own app, the Trusona identifier will be the `userIdentifier` that you used when you activated the user's device. If you registered the user using their ForgeRock ID then the mapping will automatically work. If you registered the user's email address, you can set it up to use `mail` and it will work the same as the Trusona App. Finally, if you are using a different type of identifier, you will need to ensure that the ID you registered with Trusona is represented as a field in the user's ForgeRock profile and the appropriate LDAP attribute is used as a search alias. One example way to do a custom mapping like this would be to add your identifier to the user's `User Alias List` in their profile, and then configure the `Alias Search Attribute Name` to use the LDAP attribute `iplanet-am-user-alias-list`.

![Using an alias to map users](./images/search-alias-by-alias-list.jpg)

![Setting the alias](./images/user-alias-list.jpg)

Alternatively, you could also consult the OpenAM documentation to add [custom profile attributes](https://backstage.forgerock.com/docs/am/5.5/maintenance-guide/#sec-maint-datastore-customattr) to store your identifier.


## Usage

The module comes with a front end JavaScript app that handles the rendering of TruCodes, and redirecting users to their Trusona supported mobile apps when used with ForgeRocks' XUI. The following sections describe how the module works for both Desktop and Mobile users.

### Desktop Users

Desktop users will see a TruCode in their browser that they will need to scan with their Trusona enabled mobile app. They will then receive a Trusonafication, which they can either accept or reject. If they accept, the module will log in the user.

### Mobile Users
On mobile devices, instead of seeing a TruCode, to users will be deeplinked into thier Trusona enabled mobile app, where they will be presented with a Trusonafication. Once accepted, the app will send the user back to the browser, where they will be logged in.

### Programmatic Usage

You can use the ForgeRock REST API to authenticate users with Trusona instead of with XUI. Your first call to the REST API, will provide you with a ScriptTextCallback, and three HiddenValueCallbacks. These are used by XUI and other browser based implementations to display a TruCode to the user. You can ignore them, and simply send the response back to the API to proceed to the next stage, which will let you programmatically trusonafy a user.

There are three ways you can authenticate with the module. They are:

1. Trucode ID - Typically used when used as primary authentication in a web browser. It lets the user scan a QR code to identify themselves.
1. User Identifier - If you already know the user who is attempting to authenticate, you can challenge them via their `user_identifier`
1. Device Identifier - If you know what specific device you are communicating with, you can also authenticate via `device_identifier`

See the Trusona SDK documentation for more information about the different types of identifiers.

The module requires two callbacks to be completed. One is a `ChoiceCallback` that tells Trusona which of the three identifiers above are being used to identify the user. The second is a `NameCallback` that tells Trusona what the value of the identifier is.

Here is an example API response for the Trusona Authentication Module that shows the required callbacks.
```json
{
   "authId": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJvdGsiOiJudnBrbGpjY3BxcHZrNjJ1dG00NjE2NWk1bCIsInJlYWxtIjoibz10cnVzb25hLG91PXNlcnZpY2VzLGRjPW9wZW5hbSxkYz1mb3JnZXJvY2ssZGM9b3JnIiwic2Vzc2lvbklkIjoiOHBRa2JLOUgxakdSdGd6c1NnSjVMa0ctR3RjLipBQUpUU1FBQ01ERUFBbE5MQUJ4V1dsRklhV0o1UVZrdkwzSlFlRWRJTTA5MFJERkNWV1JLYVdzOUFBSlRNUUFBKiJ9.4E_fhOxiz6JQafr3hCN0YdPHA2r3VbRJ6NmE0y2_V3Y",
   "template": "",
   "stage": "TrusonAuth1",
   "header": "Identifier",
   "callbacks": [
     {
       "type": "NameCallback",
       "output": [
         {
           "name": "prompt",
           "value": "identifier"
         }
       ],
       "input": [
         {
           "name": "IDToken1",
           "value": ""
         }
       ]
     },
     {
       "type": "ChoiceCallback",
       "output": [
         {
           "name": "prompt",
           "value": "identifier_type"
         },
         {
           "name": "choices",
           "value": [
             "user_identifier",
             "device_identifier",
             "trucode_id"
           ]
         },
         {
           "name": "defaultChoice",
           "value": 0
         }
       ],
       "input": [
         {
           "name": "IDToken2",
           "value": 0
         }
       ]
     }
   ]
 }
```
You can fill in the missing information for each callback and respond as shown below. This will block until the user accepts or rejects the Trusonafication, or if the Trusonafication expires.
```shell
curl -XPOST -H "Content-Type: application/json" $FORGEROCK_URL/json/realms/trusona/authenticate -d \
 '{
   "authId": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJvdGsiOiJudnBrbGpjY3BxcHZrNjJ1dG00NjE2NWk1bCIsInJlYWxtIjoibz10cnVzb25hLG91PXNlcnZpY2VzLGRjPW9wZW5hbSxkYz1mb3JnZXJvY2ssZGM9b3JnIiwic2Vzc2lvbklkIjoiOHBRa2JLOUgxakdSdGd6c1NnSjVMa0ctR3RjLipBQUpUU1FBQ01ERUFBbE5MQUJ4V1dsRklhV0o1UVZrdkwzSlFlRWRJTTA5MFJERkNWV1JLYVdzOUFBSlRNUUFBKiJ9.4E_fhOxiz6JQafr3hCN0YdPHA2r3VbRJ6NmE0y2_V3Y",
   "template": "",
   "stage": "TrusonAuth1",
   "header": "Identifier",
   "callbacks": [
     {
       "type": "NameCallback",
       "output": [
         {
           "name": "prompt",
           "value": "identifier"
         }
       ],
       "input": [
         {
           "name": "IDToken1",
           "value": "caf2c89c-77ce-4e82-8c4f-e900aae63e94"
         }
       ]
     },
     {
       "type": "ChoiceCallback",
       "output": [
         {
           "name": "prompt",
           "value": "identifier_type"
         },
         {
           "name": "choices",
           "value": [
             "user_identifier",
             "device_identifier",
             "trucode_id"
           ]
         },
         {
           "name": "defaultChoice",
           "value": 0
         }
       ],
       "input": [
         {
           "name": "IDToken2",
           "value": 2
         }
       ]
     }
   ]
 }'
```


