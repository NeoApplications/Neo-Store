CHANGELOG
=========

1.1.0 (XX.07.2025) +180 Commits +70 Translations
------------------

### Function

- Add: Initial support for F-Droid's Index-V2
- Add: Preference to enable Index-V2
- Add: Ktor ContentEncoding client plugin
- Add: Repositories interface for better domain management logic
- Add: Insert repository categories & anti-features to database (index-v2)
- Add: Support index-v2's webBaseUrl for the respective repos
- Add: Support for OEM/ROM-preset repositories
- Add: Mirror rotation support for repo sync and apk downloads
- Add: Preference for maximal number of idle connections
- Add: Install tasks restarter on running the activity
- Add: Retries and backoff for InstallWorker
- Add: RB badge to release items
- Add: RB logs provider preference
- Add: Support for index-v2 incremental updates
- Add: Index-v2 merger test suite
- Add: Pull trackers or rb-logs only if modified since last sync
- Add: Debug notification utility
- Fix: Selected releases order of EmbeddedProducts
- Fix: Crashes on update where some releases, tasks or downloads stuck in the database
- Fix: Improve update logic of getting compatible releases and preferred one for update
- Fix: Returning products of the repo with lowest id instead of highest versionCode
- Fix: Exporting extras
- Update: Revamp InstallWorker improving its failure handling
- Update: Refactor BaseInstaller to use InstallQueue and InstallStateHolder providing common logic for all installers
- Update: Revamp all installers improving their robustness
- Update: Simplify startUpdate() logic
- Update: Revamp Product and its DAO to v2
- Update: Improve the main products' query (should provide a big performance boost)
- Update: Fit icon and screenshot uri generators to fit the new logic
- Update: Keep name and description if updated repo lack values
- Update: Make main non-flow Dao functions suspend
- Update: Store full signing history in Installed
- Update: Migrate logics of App page into its VM
- Update: Improve developer's other apps' query logic
- Update: Replace Screenshot items with their paths
- Update: Revamp the privacy-processor to handle repo anti-features (index-v2)
- Update: Revamp database instance creator call
- Update: Run restart on theme change in a main coroutine
- Update: Enable R8's full mode
- Update: Enlarge download buffer size
- Update: Make stateflows flow only while subscribed
- Update: Clean most DAOs logics
- Update: Simplify coil call handler logic and caching
- Update: Default to enabling Index-v2
- Update: Improve installs queueing with checks, timeouts and retry counters
- Update: Remove specific file types from cache where possible (fixes deleting external download folder)
- Remove: Preference for number of searched apps
- Add repository: Brave browser
- Remove repository: Frostnerd, Frostnerd-archive and Libretro
- TargetSDK 35
- CompileSDK 36

### UI/UX

- Add: Baklava android version name
- Add: Clear positive action on permission cards
- Add: Index's real timestamp to repository page
- Fix: Missing buffer on the bottom of the repos list
- Fix: Respect set app language first, then system when parsing localized index values
- Fix: Double v's in the version name chip in app page
- Fix: Showing Error on the progress bar when starting download
- Fix: Showing apps with one release but different arch as new too
- Update: Revamp selection chips layout
- Update: Show localized categories and anti-features (index-v2)
- Update: Revamp search bar layout
- Update: Revamp search page bar layout
- Update: Revamp updates' card layout
- Update: Revamp ActionButton layout
- Update: Use double carets for expandable cards
- Update: Revamp repository page layout
- Update: Default theme primary and secondary colors

1.0.9 (07.04.2025) +20 Commits +20 Translations
------------------

### Function

- Add: A singleton koinNeoViewModel
- Update: Improve Main panes navigation logic
- Update: Migrate DownloadedDao usage to a repository
- Update: Revamp DownloadWorker logic, notifications and handling
- Update: Revamp SyncWorker logic, notifications and handling
- Update: Reduce Ktor logging level
- Update: Move panes navigation and search query logics to MainVM

### UI/UX

- Add: Handling of opening an app page from system app info
- Fix: Notification following system language and not the app's
- Remove: Duplicate sync notification channels
- Update: Use default navigation icon or text size

1.0.8 (16.02.2025) 9 Commits 0 Translations
------------------

### Function

- Fix: Crash on stopping download
- Fix: Crash on signal flow closing
- Fix: Liberapay donation link (credits @leoheitmannruiz)
- Update: Improve consuming sync and download state updates
- Add: User agent to calls in all clients
- Update: Use IO instead of Default dispatcher for workers

1.0.7 (14.02.2025) +20 Commits +80 Translations
------------------

### Function

- Fix: Not showing install on packages from sources without share
- Fix: Recomposing navigation suite on changing pager state
- Fix: Parsing and showing litecoin and liberapay links
- Update: Improve installer reliability
- Update: Limit parallel running SyncWorks
- Update: Improve actions flow logic
- Remove: Flattr donation link parsing

### UI/UX

- Add: Support for tv & wear screenshots
- Add: Support for video link
- Add: Dialog to notify for intial sync on first launch
- Update: Revamp app info cards

1.0.6 (23.01.2025) +140 Commits +100 Translations
------------------

### Function

- Add: Delete the downloaded file when erasing the download entry
- Add: Break download and clean when exceeding total file size
- Add: Option to disable SSL validation
- Add: Worker for batch syncs
- Fix: Validation of repo authentication values
- Fix: Editing of current values of repo
- Fix: Make Downloaded keys fully unique
- Fix: Installs queueing logic
- Fix: Performance issues of SyncWorker
- Fix: Back handling in Main pages
- Fix: Crash on missing stopReason
- Fix: Lazy load of installed apps
- Fix: Fetching icons/screenshots also respect disabling ssl certificate check
- Fix: New installed app isn't being added to homescreen (credits @Dhina17)
- Update: Improve search algorithm
- Update: Automate V1-parsing using IndexV1
- Update: Rewrite IndexV0 xml parser
- Update: AppSheet header logic
- Update: Revamp updating extras logic (reduce latency)
- Update: Koin-inject Main & Prefs ViewModels
- Update: Replace the multiple VMs for single MainVM
- Update: Revamp WorkerManager & all Workers
- Update: Revamp WorkerManager handling of downloads & syncs
- Update: Migrate to type-safe navigation
- Update: Indices and naming of Room Entities
- Update: Improve performance of the main queries
- Update: Improve flows' memory & performance in MainVM
- Update: Improve f-droid sharing link (credits @leoheitmannruiz)
- Update: Avoid request for install permissions when INSTALL_PACKAGES is granted (credits @tharowt)
- Remove: Unused BootReceiver
- Add Fcitx5, IronFox repos (credits @Integral-Tech, @celenityy)
- Remove Bromite, INVISV, Funkwhale, Divolt, DivestOS, Mobilsicher and the archive repos
- CompileSDK 35

### UI/UX

- Add: Target- & MinSDK filters
- Add: Erase-all button to downloads page
- Add: Indicator of the invalid field in repo editor
- Add: Info card in other prefs page
- Fix: Folding full app description
- Fix: Chips visibility independent of background pref
- Fix: Text overflows and stacking of release labels
- Fix: Text padding in action buttons
- Update: Make UI wide screens friendly & navigation adaptive
- Update: Revamp explore, search & repo pages
- Update: Restructure prefs hierarchy
- Update: Revamp block borders' design and make default
- Update: Show text always on alternative navigation button
- Update: Animate permission cards
- Update: Highlight version update chip
- Update: Revamp dialog layouts
- Update: Revamp icons & screenshots state creation
- Update: Improve install & validation state notifications
- Update: Animate transition between categories' layouts
- Update: Revamp main theme colors
- Update: Make preference's surface and switch interactively connected

1.0.5 (19.07.2024) +10 Commits +10 Translations
------------------

### Function

- Add: Debounce on downloaded flow
- Add: RepoId to Downloaded for a more unique key
- Fix: SortFilter sheets across pages
- Update: Init Koin earlier
- Remove: All archive repositories

### UI/UX

- Add: Confirmation dialog on root uninstaller too
- Add: Long tap sync button for latest successful sync info

1.0.4 (09.07.2024) -10 Commits
------------------

### Function

- Add: Indexes to Room entities
- Add: None Source & Request
- Fix: A memory leak
- Update: Revamp Request

### UI/UX

- Remove: All from explore page

1.0.3 (06.07.2024) 30+ Commits +30 Translations
------------------

### Function

- Fix: BackHandler of AppSheet
- Fix: Crash on launching from permissions' page
- Fix: Crash on using same key for lazy release items
- Fix: Opening different prefs page when default page is changed
- Update: Inject installer using Koin
- Update: Migrate most sheets to BottomSheetScaffold
- Update: Make flows respect distinction earlier
- Update: Revamp Source handling of requests

### UI

- Update: Convert Search back to a page
- Update: Revamp Installed page hierarchy
- Update: Use segmented buttons in Installed page
- Update: Revamp apps' carrousel and limit version name length

### UX

- Add: Special search filter tabs
- Add: Indicator that an app is installed to list items
- Update: Default to search all apps

1.0.2 (19.05.2024) 10+ Commits +50 Translations
------------------

### Function

- Fix: Crash on tapping install notification of session installer
- Fix: Excessive recomposition on Repo apps list
- Update: Increase default search apps to 2000

### UI

- Add: Option to show Android version name instead of SDK (enbaled by default)
- Update: Add enable and dismiss buttons to RepoSheet

### UX

- Add: Dialog on clicking links from app descriptions or changelogs
- Add: Parsing markdown links in descriptions or changelogs
- Add: Search bar to repos page

1.0.1 (09.05.2024) 30+ Commits +20 Translations
------------------

### Function

- Add: Safeguard to avoid spamming sync
- Add: Option for limiting loaded apps for search
- Add: Support of reproducible builds (credits @obfusk & @iamlooker)
- Fix: Loading apps without categories
- Fix: Disable Google-signed dependency metadata (credits @IzzySoft)
- Update: Revamp usage of CoroutineDispatchers
- Update: Increase default auto-sync interval
- Add Repos: Cloudburst & Huizengek (credits @GitGitro)
- Remove Repo: Samourai Wallet (credits @GitGitro)

### UI

- Add: Indicator if a custom sort/filter is applied
- Add: All apps category
- Fix: Constant recomposition of most UI (reduce Jitter/Lag)

### UX

- Add: Launch AppSheet when tapping installed notification
- Fix: Screenshots not updating when changing app of same dev
- Fix: Handling locales with three letters as language code
- Update: Improve consistency of sheets' visibility across rotation
- Update: Increase default recently updated apps to show

1.0.0 (23.04.2024) 440+ Commits +380 Translations
------------------

### Function

- Add: Preference to disable signature check for updates/installs
- Add: Preference to disable download version check
- Add: Preference to allow downgrades (debuggable) and installing pre-A6 apps (starting Android 14)
- Add: Display localized product name and description (credits @BLumia)
- Add: Export/import the repositories
- Add: Export/import list of installed apps
- Add: Room Kotlin KSP code generation
- Add: ConnectionPool & redirections to Coil calls
- Add: Opening IzzyOnDroid links (credits @GitGitro)
- Add: Network constraint to Download works
- Add: Own client & connectionPools for CoilDownloader
- Add: ForegroundServiceType for works (Required on A14)
- Add: Package source to session installer (SDK33+)
- Add: Explicit field for HTTP proxy address
- Add: Option to use DownloadManager API
- Add: AppManager installer
- Add: System installer (credits @0x50f13)
- Add: Linking database from Application
- Fix: Calling Tor addresses
- Fix: Make RootInstaller null-safe
- Fix: Auto-sync on "Only charging"
- Fix: Adding repo using scanner
- Fix: Detecting updates of multi-repos apps
- Fix: Clicking updates notification
- Fix: Launching parallel updates
- Fix: Updating apps in the background
- Fix: Install notification
- Fix: External search calls
- Fix: Opening an app from repo url
- Fix: Crash on searching special chars
- Update: Rebase to use one activity
- Update: Replace RxJava usage with Coroutines
- Update: Replace OkHttp with Ktor (except for Coil)
- Update: Migrate download, exodus & sync backend from Sevice- to Worker-based
- Update: Convert InstallerService to BroadcastReceiver
- Update: Revamp the ExodusAPI classes
- Update: Respect version code and source ("fdroid") when fetching ExodusInfo
- Update: Move ExodusInfo fetching into AppSheet
- Update: Allow parallel downloads from different repositories
- Update: Allow parallel sync of repos
- Update: Cache apks in external cache
- Update: More open network security policy
- Update: Make root session installer default if A13+
- Update: Make product queries observe all relevant entities
- Update: Optimize the main products query
- Update: Revamp all installers
- Update: Revamp Installer works
- Update: Revamp Downloader
- Update: Revamp proxy handling
- Update: Revamp Repository data class
- Update: Differentiate between double and single list VMs
- Update: Replace appsToBeInstalled list usage with InstallWorker
- Update: Allow pre-composing all pages
- Update: minSDK to 24
- Update: targetSDK & compileSDK 34
- Remove: Dependencies of deprecated usage
- Add Repos: Aniyomi, Koyu, Kuschku, Kvaestiso, Etopa, Metatrans Apps, Gadgetbridge, FUTO, Grobox,
  Julian Fairfax, spiritCroc Test, Zimbelstern, Cromite, Divolt Repo, Rboard, Cake Wallet (credits
  @GitGitro)
- Add Repos: Samourai Wallet Repo (credits @RequestPrivacy)
- Add Repos: Julian Andres Klode's repo (credits @ishanarora)
- Add Repos: PurpleI2p, INVISV, Monerujo, iodé, spiritCroc, DivestOS Unofficial, Funkwhale… +28
  other
- Fix Repos: The description for PeterCxy's Shelter repo (credits @ishanarora)
- Fix Repos: c:geo repository (credits @ishanarora)
- Fix Repos: Cromite repository address
- Remove Repos: Fluffy Chat (stable and nightly), Ungoogled Chromium, i2p

### UI

- Add: Preference to show/hide categories bar
- Add: (Products)List, StringInput DialogUIs
- Add: Block border to sheets & pages
- Add: Categories app info chips
- Add: New apps carousel layout
- Add: Optional alternative BottomNavBar item layout
- Add: Optional alternative new apps layout
- Add: Dynamic theme variants
- Add: Medium and high contrast themes
- Add: Option to hide the new apps bar
- Fix: Switch layout on low-dpi/multi-lines
- Fix: Screenshots scaling issue after opening once
- Fix: Pre-mature cutting horizontal products recycler layout
- Fix: Avoid forgetting expand-state of description text
- Fix: Ghost clicking settings when search is expanded
- Fix: Hectic sorting of downloads in downloads list
- Update: App's icon
- Update: Theme colors (MD3 v2/tonals)
- Update: Split Installed page to two tabs
- Update: Revamp repositories page
- Update: Make pagers animate scrolling
- Update: Larger layout corners
- Update: Revamp all pages
- Update: Revamp all sheets (now composables)
- Update: Revamp all dialogs (now composables)
- Update: Revamp all items (using ItemList)
- Update: Revamp all components
- Update: Revamp screenshots layout
- Update: Resort Main pages
- Remove: Scrollable top bars
- Remove: Option to hide categories bar

### UX

- Add: Permission ignore button
- Add: Kids mode
- Add: Apps list to Repository info
- Add: Qr Code to repo sheet
- Add: PrivacyPoints to PrivacyPanel cards
- Add: Licenses filters
- Add: Preference to keep notification of successfully installed app
- Add: Swipe down to close gesture to screenshots viewer
- Add: Swipe to navigate between pages
- Add: Singular search page
- Add: (De)select all buttons to multi-selection filters
- Add: Launch search page on receiving search Intent
- Add: More categories icons
- Add: Erase button to items in downloads log
- Add: Preference to allow unstable updates to each app
- Add: All Anti-features in PrivacyPanel
- Add: Permission to install packages
- Add: Search when to-be-opened package doesn't exist
- Add: Option to confirm before downloading an app
- Add: Device or biomteric lock options on download dialogs
- Fix: Showing updatable label for Product items
- Fix: Showing right version in AppSheet's header
- Fix: Showing downloaded version (in downloads log)
- Update: Differentiate between having no trackers and lacking exodus data
- Update: Convert auto-sync interval unit to hours
- Update: Alphabetically sort current downloads in Installed page
- Update: Hide inactive sort/filter options
- Update: Communicate status on download errors
- Update: Bind short description lines limit to extended state
- Update: Show short description for unexpanded description
- Update: Using pager in App Sheet for Privacy Panel
- Update: Better download state communication
- Update: Revamp download and sync notifications
- Update: Use categories grouping in Explore page
- Update: Prefer search results with last update not longer than 1 year ago
- Update: Text to show when no compatible releases exist
- Update: Show dialog before opening source code
- Remove: Search bar from Main pages
- Remove: Zooming on screenshots

0.9.15 (01.03.2023) 40+ Commits +30 Translations
------------------

### Function

- Add: KDE (Stable), C:Geo, Stack Wallet & PeterCxy's Shelter repos
- Add: Option for external download folder
- Fix: Background (silent) installer
- Fix: Cache clean up time in days not hours
- Update: Navigating to AppSheet without input of dev name
- Update: Simplify apps list in ExplorePage

### UI

- Fix: Expandable layout's padding
- Update: Move favorite button beside main action
- Update: BottomNav, SideNav layouts
- Update: Smaller layout icons

### UX

- Add: Permissions page
- Add: Downloads page (switch) to InstalledPage
- Add: Option to ignore notification on security vulnerabilities
- Update: Scroll to top when changing category in ExplorePage
- Update: Increase showing new & updated apps limit
- Update: Improve preference dialog value setting

0.9.14 (07.02.2023) 120+ Commits +80 Translations
------------------

### Function

- Fix: Not detecting root permission
- Fix: Unbinding DownloadService crash
- Fix: Crash on rxjava's throwable nullability
- Fix: Avoid calling https://icon on apps sans icon
- Fix: Save capitalized Repo fingerprint
- Update: Revamp batch update
- Remove: Obsolete Sections
- Remove: Unused Gradle options

### UI

- Update: Revamp InstalledPage
- Update: Revamp BottomNavBar button

### UX

- Add: Option to disable getting/showing trackers
- Add: FluffyChat nightly, Anonymous Messenger and BeoCode to default repos
- Add: Tooltip when trackers data isn't available
- Fix: FluffyChat repo address and fingerprint
- Fix: Not updating installed apps list on (un)installing apps
- Fix: Sticky sync/download notification
- Update: Enable Guardian project's repo by default

0.9.13 (20.01.2023) 90+ Commits +60 Translations
------------------

### Function

- Add: Repository's authentication pair setter/getter
- Add: Job IdRange for Sync jobs
- Add: DownloadState to SyncService
- Add: Sync/Download foreground service types
- Add: Option for sheets to be not pre-extended
- Add: Export/import of extras
- Add: SAF-File (with read, write, delete and share)
- Fix: Improve performance overload caused by search (credits @SocratesDz)
- Fix: Crashing LegacyInstaller (again)
- Fix: Fall back to default installer if root not granted
- Update: Use DownloadService's mutex to keep the service alive while needed
- Update: Replace RxJAva usage with Coroutines in SyncService
- Update: Allow using Coroutine Job as disposable
- Update: Revamp scheduling auto-sync
- Update: Improve installer handling based on existing in background and type
- Update: Simplify Prefs listener
- Update: auto-sync interval limits
- Update: Migrate last LiveData to Flows
- Update: FluffyChat's repo address
- Remove: Unused Dependencies

### UI

- Add: Cleaner splash screen icon
- Add: Filter Download state changes percentually
- Add: Warning card to AppSheet on known vulnerability
- Add: Repo mirror chips row
- Fix: 2-rows updated apps block even on one item
- Update: Migrate repo sheets into one composable
- Update: Snackbar stays shorter

### UX

- Add: Auto-sync option on charging
- Add: Active downloads list to Installed
- Add: Description text to ActionButtons (for screen readers, needs testing)
- Add: Show notification if an installed app has known vulnerabilities
- Add: Make Preferences accessible from system's app info
- Add: Allowed anti-features filter
- Fix: Unable to add a repository after adding empty one
- Fix: Running auto-sync only on WIFI, on plugged-in and combined
- Fix: Crash on deleting newly added repo
- Update: Open exodus tracker's page instead of tracker's website

0.9.12 (28.12.2022) 30+ Commits +60 Translations
------------------

### Function

- Add: Notifications permission (for A13)
- Add: Ask for PostNotifications permission on start
- Fix: Improve search performance (credits @pedrox-hs)
- Fix: Showing apps of all developers with protonmail as apps from the protonmail developer
- Fix: Sorting installed apps
- Fix: Replace deprecated functions in RootInstaller
- Fix: Installers getting false context
- Fix: LegacyInstaller's false Uri
- Update: TargetSDK 33

### UI

- Fix: Location permission group icon
- Update: ProductCard layout
- Update: Horizontal Product recycler layouts

### UX

- Add: Categories SideNavBar to explorer
- Fix: Category SideBar not updating according to SortFilterSheet
- Update: Change category filter to single selection
- Update: Asking for root permission when setting root installer

0.9.11 (03.12.2022) 70+ Commits +15 Translations
------------------

### Function

- Fix: Memory leak in Main & Prefs

### UI

- Add: The all new Privacy Visualization (Neo Meter Icons)
- Add: The all new Privacy Panel

### UX

- Fix: Opening the same RepoSheet repeatedly

0.9.10 (22.11.2022) 230+ Commits
------------------

- Add: App icon based on system theme
- Add: Installer type preference (including Legacy mode for dumb MIUI)
- Add: Specific screenshot placeholder
- Add: Cryptomator & TwinHelix Signal-FOSS preset repositories
- Fix: Navigation after launching from updates notification
- Fix: Download bar not reflecting true progress
- Fix: Prefs' dependency handling
- Update: Empty & hide SearchBar on pressing back before exit
- Update: ViewModels as monotone instances in the Activities
- Update: Simplify ListProduct layout

0.9.9 (31.10.2022) 7 Commits
------------------

- Add: Scan repo QrCode using BinaryEye (if installed)
- Fix: Lock in Installed after launching from updates notification
- Fix: Improve handling intent

0.9.8 (27.10.2022) +60 Commits
------------------

- Add: Support for fdroidrepo:// intents (simple addition of repositories)
- Add: Support for market://search? intents (used by some launchers)
- Add: Share option for IzzyOnDroid apps
- Add: GitJournal & Revolt to the preset-repos
- Update: Highlight apps with available updates*
- Update: Show current version if app installed instead of latest
- Update: Avoid killing the app on pressing back in Main
- Update: SortFilterSheet's reset, resets prefs to default
- Update: Improve detecion of sort/filter changes
- Update: Revamp ReleaseItems
- Update: Revamp search backend
- Update: Replace LiveData with Flows in most instances
- Fix: Opening Installed on launching updates intent
- Remove: Alefvanoon preset-repo

0.9.7 (11.10.2022) +50 Commits
------------------

- Add: Other apps from the developer
- Add: Scrolling TopBar
- Add: Rest of the data from the table to query return
- Fix: Some coloring issues of the dynamic theme
- Update: Search also searches among author names
- Update: New screenshots viewer (fixes previous issues and allows zooming)
- Update: Allow two-lines labels in ProductCard
- Update: Bigger app icons
- Update: Some layouts e.g. TopBar, SortFilter
- Update: Make StatusBar and gestures bar transparent
- Update: Revamp theming backend (fixes some visuals)
- Update: Set default theme based on Android version
- Update: Improve Sheets' initialization
- Update: Dispose of composable when sheet fragment is destroyed

0.9.6 (05.10.2022) +90 Commits
------------------

- Add: Sort/Filter sheet
- Add: Explicit favorites shortcut
- Add: TargetSDK to info chips
- Update: Replace Material icons with Phosphor
- Update: Replace drawables with composables
- Update: BottomNavBar, AppSheet's and EditRepoSheet's layouts
- Fix: Sheets' animated background
- Fix: Showing update secondary action while downloading
- Fix: Secondary actions being squeezed on low dpi
- Fix: Immersive bottom gesture bar (except with sheets)
- Remove: Legacy/now-unused resources

0.9.5 (19.09.2022) +120 Commits
------------------

- Add: Useful links in Prefs
- Update: Revamp AppSheet's Head & Header
- Update: Repo item's text colors
- Update: Migrate navigation fully to Compose
- Update: Migrate preferences to Compose
- Update: Allow setting higher number for updated/new apps' recycler and sync interval
- Fix: ActionButtons visibility
- Fix: Show Repo description pre-first-sync
- Remove: Legacy fragment-related classes

0.9.4 (18.08.2022)
------------------

- Fix: Showing updates notification even when disabled
- Add: Threema, Stable Calyx OS and Session to preset-repositories
- Fix(try): Background installer
- Add: Release apks and images cache retention options
- Update translation

0.9.3 (06.08.2022)
------------------

- Add: Favorites category
- Add: Option to set sync interval
- Add: Supporting auto importing fdroidrepos: repo addresses from clipboard
- Fix: Nested scrolling of AppSheet
- Fix: Repository apps count
- Fix: Allow retention of apk till "consumed"
- Update: Make app description and changelog text selectable
- Update: Show full text of inexpandable text blocks
- Small UI fixes

0.9.2 (28.07.2022)
------------------

- Fix: Setting ignore updates doesn't hide from updates
- Fix: Alphabetical sorting of Installed
- Fix: Nested scrolling of AppSheet
- Fix: Showing install action while download is running
- Fix: StatusBar icons visibility on dynamic colors' theme
- Update Translations

0.9.1 (10.07.2022)
------------------

- Add: System dynamic theme (aka. Material You)
- Add: 7 new preset repositories
- Add: Architecture label on releases for a specific architecture
- Remove: UnifiedPush repo (dead)
- Fix: Links in settings
- Fix: Annoying 0 updates notification
- Fix: Hide Author's website when it isn't available
- Fix: Parsing clipboard's content for repo URI
- Fix: Not showing update action

0.9.0 (07.07.2022)
------------------

- Renamed package to com.machaiv3lli.fdroid
- Fully new UI/UX with Material Design 3
- Add: Update all
- Add: Favorite (further features to come later)
- Add: Snackbar with action on long press actions
- Add: Flexibel extended card actions
- Add: Expandable extra actions for AppSheet
- Add: Option to not show screenshots
- Add: Cross-compatibility of root and normal installer
- Add: Language preference
- Add: IPv6 Support with fast fallback to IPv4 (@Mynacol)
- Add: Session-root-installer option (Needed for +A13)
- Add: Only ask for root when selected
- Update: Show repository host name instead of developer name on repository card
- Update: horizontal scrolling screenshots
- Fix: Auto updating installed/updates lists
- Fix: Crash on A12+ (gotta disable battery optimization)
- Fix: Root installer
- Fix: Delete Cached File After Uninstall
- Fix: Crash on pressing download when offline
- Remove: Unused list animation preference

0.4.3 (20.12.2021)
------------------

- Fix: Screenshot placeholder icon
- Fix: Soft crash when starting AppDetails
- Fix: Run only one instance of root installer
- Fix: Root installer

0.4.2 (15.12.2021)
------------------

- Update: More of Material Design 3
- Clean up: Huge parts of the code base
- Update: Optimize installer
- Fix: Delete Cached File After Uninstall
- Fix: Freeze with Root Installer
- Fix: Links' icon tint
- Fix: Crash on pressing download when offline
- Fix: Accidental scrolling on back gesture
- Fix: Screenshots size before loading
- Fix: Action Text Color visibility
- Fix: App version text being colored when not planed
- Update translations: Hindi, Hungarian, Finnish, Norwegian, Chinese(Simplified), Turkish, Russian,
  Portoguese(Brazil), German, French, Italian, Arabic (Saudi Arabia), Spanish, Sinhala, Polish,
  Dutch, Greek

0.4.1 (17.11.2021)
------------------

- Fix Normal Install Crash

0.4.0 (17.11.2021)
------------------

- Add: Language preference
- Update: horizontal scrolling screenshots
- Fix: Background root installer
- Add: Only ask for root when selected
- Tons of UI/UX tweaks

0.3.6 (28.10.2021)
------------------

- Add Icon for opening Details
- Top App Bar Differences made more prominent
- Root Permission will only be asked when used
- Switch Visibility fixes
- Added Arabic Translation
- Long press on source Icon to copy link
- Replace Toast with Snackbar
