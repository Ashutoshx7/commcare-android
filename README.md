# commcare-odk

To set up an Android dev environmnet for commcare-odk, do the following:

- Download [Android Studio](https://developer.android.com/sdk/index.html)
- Make sure you have java installed

Go ahead and open Android Studio if this is your first time using it;
it may take you through some sort of setup wizard, and it's nice to get that out of the way.

Android Studio's default project space is `~/AndroidStudioProjects` so I'm going to use that in the example.
CommCare ODK depends on CommCare and JavaRosa, and CommCare ODK expects the three of them to live side by side
in your directory structure. You can acheive this with the following commands (in bash):

```bash
cd ~/AndroidStudioProjects
mkdir CommCareODK
cd CommCareODK
git clone https://github.com/dimagi/commcare-odk.git
git clone https://github.com/dimagi/commcare.git
git clone https://github.com/dimagi/javarosa.git
```

- Open Android Studio
- From the Android Studio Welcome dashboard, click "Import project (Eclipse ADT, Gradle, etc.)"
- Select AndroidStudioProjects > CommCareODK > commcare-odk and hit OK
- Click "Cancel" on the Gradle Sync modal
  (this just declines Android Studio's overzealous offer to do some automatic stuff we don't want)
- Click "Cancel" on the Import Gradle Project popup (also just declining something automatic that we don't want)
- Wait while Android Studio spins its wheels

## Running CommCare ODK

Now you're basically ready to go. To build CommCare ODK and get it running on your phone,
plug in an android phone that

- is [in developer mode has USB debugging enabled](https://developer.android.com/tools/device.html#setting-up)
- doesn't have CommCare ODK installed on it

Alternatively, you can resign yourself to using the android emulator on your computer,
but that will be a less pleasurable experience.

In Android Studio, hit the build button (a green "play" symbol in the toolbar).
The first build will take a minute.
Then it'll ask you what device to run it on; select your device (or the emulator).

Enjoy!
