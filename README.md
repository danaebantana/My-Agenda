# My-Agenda

## About
We designed a mobile application called 'My Agenda' using Android Studio and Firebase to create and arrange appointments/events. 

## Key Components
### Forms
SignUp\
Login\
Profile\
Contacts\
Event\
Navigation Bar\
Logout

### Elements
View-Edit-Add-Delete an event\
Speech and Voice recognition\
Send Reminder SMS

* SignUp, Login, and Logout procedures are achieved using the email-password authentication method provided by Firebase.
* The actions (View, Edit, Add, Delete) on an event are attained through Firebase Realtime Database procedures using DatabaseReference instances.
* Each user has to add their personal information to be able to create events.
* With the use of SQLiteDatabase, each user can have several contacts stored locally in their phone.

## Event Details
Firstly, for each event a date and time must be set. Additionally, the creator can set the duration of the event and add collaborators from their contacts stored on their phones. Any changes conducted by the creator of the event, are visible to everyone involved. Each collaborator must have an account to use the application. They can confirm their attendance and leave comments in events they participate in. 

## Technologies
Project is created with:
* Android Studio
* minSdkVersion 26
* targetSdkVersion 30

Project Dependencies:
* com.android.tools.build:gradle:4.1.2
* com.google.gms:google-services:4.3.5

App Dependencies:
* com.google.gms.google-services
* androidx.appcompat:appcompat:1.2.0
* com.google.android.material:material:1.2.0-alpha02
* androidx.constraintlayout:constraintlayout:2.0.4
* com.google.android.gms:play-services-maps:17.0.0
* com.google.firebase:firebase-config:20.0.3
* com.google.android.gms:play-services-location:18.0.0
* testImplementation 'junit:junit:4.+'
* androidTestImplementation 'androidx.test.ext:junit:1.1.2'
* androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
* com.google.firebase:firebase-bom:26.4.0
* com.google.firebase:firebase-auth
* com.google.firebase:firebase-database
* com.google.firebase:firebase-storage
