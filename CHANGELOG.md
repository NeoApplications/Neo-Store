CHANGELOG
=========

0.9.12 (28.12.2022) 30+ Commits +60 Translations
------------------

### Function
- Add: Notifications permission (for A13)
- Add: Ask for PostNotifications permission on start
- Fix: Improve search performance (credits @Pedrox)
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
- Update translations: Hindi, Hungarian, Finnish, Norwegian, Chinese(Simplified), Turkish, Russian, Portoguese(Brazil), German, French, Italian, Arabic (Saudi Arabia), Spanish, Sinhala, Polish, Dutch, Greek

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
