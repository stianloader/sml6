package org.stianloader.sml6.starplane.autodeobf;

public interface StarmappedNames502 {

    // --- Package Declarations ---

    /**
     * The name of the package where almost all galimulator-related classes are located in.
     * There is also the test package, the namegenerator package and the com.example package,
     * however they are not the package described by this string.
     */
    public static final String BASE_PACKAGE = "snoddasmannen/galimulator/";
    public static final String DIALOG_PACKAGE = BASE_PACKAGE + "dialog/";
    public static final String UI_PACKAGE = BASE_PACKAGE + "ui/";
    public static final String GUIDES_PACKAGE = BASE_PACKAGE + "guides/";
    public static final String RENDERSYSTEM_PACKAGE = BASE_PACKAGE + "rendersystem/";

    // --- Class/Interface Declarations ---

    public static final String ABOUT_WIDGET_CLASS = UI_PACKAGE + "AboutWidget";
    public static final String ABSTRACT_BULLETIN_CLASS = BASE_PACKAGE + "AbstractBulletin";
    public static final String AUXILIARY_LISTENER_CLASS = "snoddasmannen/galimulator/AuxiliaryListener";
    public static final String ACTOR_SPAWNING_PREDICATE_CLASS = BASE_PACKAGE + "Space$ActorSpawningPredicate";
    public static final String BASIC_BUTTON_CLASS = BASE_PACKAGE + "ui/BaseButtonWidget";
    public static final String CONFIGURABLE_PREFERNCE_INTERFACE = BASE_PACKAGE + "ConfigurablePreference";
    public static final String DIALOG_BUTTON_CLASS = BASE_PACKAGE + "dialog/DialogButton";
    public static final String DIALOG_COMPONENT_INTERFACE = BASE_PACKAGE + "dialog/DialogComponent";
    public static final String DIALOG_INTERFACE = BASE_PACKAGE + "Dialog";
    public static final String EMPIRE_BULLETIN_CLASS = BASE_PACKAGE + "EmpireBulletin";
    public static final String EMPIRE_EXTENSION_CLASS = BASE_PACKAGE + "EmpireExtension";
    public static final String FLAG_OWNER_INTERFACE = BASE_PACKAGE + "FlagOwner";
    public static final String FLOW_BUTTON_CLASS = UI_PACKAGE + "FlowButtonWidget";
    public static final String GALAXY_PREVIEW_WIDGET_CLASS = BASE_PACKAGE + "ui/GalaxyPreviewWidget";
    public static final String GALEMULATOR_INPUT_PROCESSOR_CLASS = BASE_PACKAGE + "Galemulator$10001";
    public static final String LABELED_CHECKBOX_COMPONENT = DIALOG_PACKAGE + "LabeledCheckboxComponent";
    public static final String LABELED_STRING_CHOOSER_COMPONENT = DIALOG_PACKAGE + "LabeledStringChooserComponent";
    public static final String LANDMARK_MANAGER_CLASS = GUIDES_PACKAGE + "LandmarkManager";
    public static final String MOTTO_GENERATOR_CLASS = BASE_PACKAGE + "MottoGenerator";
    public static final String NINEPATCH_BUTTON = BASE_PACKAGE + "ui/NinepatchButtonWidget";
    public static final String ODDITY_BULLETIN_CLASS = BASE_PACKAGE + "OddityBulletin";
    public static final String PAGINATED_WIDGET_CLASS = UI_PACKAGE + "PaginatedWidget";
    public static final String PERLIN_NOISE_GENERATOR_CLASS = BASE_PACKAGE + "PerlinNoiseGenerator";
    public static final String QUAD_TREE_CLASS = BASE_PACKAGE + "QuadTree";
    public static final String RENDER_CACHE_CLASS = RENDERSYSTEM_PACKAGE + "RenderCache";
    public static final String RENDER_CACHE_COLLECTOR_CLASS = BASE_PACKAGE + "Galemulator$RenderCacheCollector";
    public static final String SETTINGS_DIALOG_CLASS = BASE_PACKAGE + "SettingsDialog";
    public static final String SETTINGS_DIALOG_BLACKLIST_BUTTON_CLASS = SETTINGS_DIALOG_CLASS + "$10001";
    public static final String SETTINGS_DIALOG_CHECKBOX_CLASS = SETTINGS_DIALOG_CLASS + "$10002";
    public static final String SETTINGS_DIALOG_STRING_CHOOSER_CLASS = SETTINGS_DIALOG_CLASS + "$10003";
    public static final String SHIP_CONSTRUCTION_WIDGET_CLASS = BASE_PACKAGE + "ui/ShipConstructionWidget";
    public static final String SHIP_CONSTRUCTION_WIDGET_LOCATION_SELECTOR_CLASS = SHIP_CONSTRUCTION_WIDGET_CLASS + "$10001";
    public static final String SHORTCUT_LIST_WIDGET_CLASS = BASE_PACKAGE + "ShortcutListWidget";
    public static final String SHOW_SHORTCUT_LIST_BUTTON_CLASS = ABOUT_WIDGET_CLASS + "$ShowShortcutListButton";
    public static final String SPACE_ACTIVE_WIDGETS_FIELD = "activeWidgets";
    public static final String SPACE_ADD_AUXILIARY_LISTENER = "addAuxiliaryListener";
    public static final String SPACE_CLOSED_WIDGETS_FIELD = "closedWidgets";
    public static final String SPACE_OPENED_WIDGETS_FIELD = "openedWidgets";
    public static final String STAR_GENERATOR_INTERFACE = BASE_PACKAGE + "StarGenerator";
    public static final String TEXT_BULLETIN_CLASS = BASE_PACKAGE + "TextBulletin";
    public static final String USER_SETTING_ENTRY_CLASS = BASE_PACKAGE + "UserSettingsEntry";
    public static final String WAR_LIST_ENTRY_CLASS = UI_PACKAGE + "WarListWidget$WarEntry";
    public static final String WAR_LIST_WIDGET_CLASS = UI_PACKAGE + "WarListWidget";
}
