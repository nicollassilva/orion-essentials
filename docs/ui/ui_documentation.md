# UI System Documentation

This documentation provides a comprehensive overview of the UI system used in this project. The UI is defined using a custom declarative language in `.ui` files, which appear to be for a game or application interface, possibly Roblox-based given the localization keys like `%server.customUI.*`.

## Overview

The UI system is hierarchical and component-based, with definitions stored in `.ui` files. Files can include other files using `$Name = "path.ui";` syntax. Components are defined with properties, and reusable styles and components are stored in  and subfolders.

### File Structure

The workspace is organized as follows:

- **Custom/**: Main UI definitions
    - : Core styles, components, and constants
    - : Sound effect definitions for UI interactions
    - **Common/**: Reusable UI components
        - : Input binding buttons
        - : Text-based buttons
        - **Buttons/**: Button-related components (not detailed in structure)
    - **Hud/**: Heads-up display elements
        - : Timer display
    - **Pages/**: Full-page UI screens
        - Various page files (e.g., , )
        - **Memories/**: Memory-related UI components
            - : Chest marker overlay
            - : Main memories interface
            - : Individual memory display
            - And more...
        - **Portals/**: Portal-related UI
        - Other specialized pages

- **Other root folders** (Crosshairs/, DamageIndicators/, etc.): Appear to contain assets but no `.ui` files in the provided structure.

## Core Concepts

### Includes
Files can include others for shared definitions:
```ui
$Common = "../../Common.ui";
$Sounds = "Sounds.ui";
```

### Component Definitions
Components are defined with `@Name = ComponentType { properties }` for reusable components, or directly as `ComponentType { properties }` for inline usage.

### Properties
Common properties include:
- `Anchor`: Positioning and sizing (e.g., `(Left: 0, Bottom: 0, Width: 40, Height: 40)`)
- `Visible`: Boolean visibility
- `Background`: Texture or color (e.g., `"path.png"` or `#000000(0.5)`)
- `Style`: References to style objects
- `Text`: String content
- `Padding`: Spacing (e.g., `(Horizontal: 10)`)
- `LayoutMode`: Layout behavior (e.g., `Left`, `Top`, `TopScrolling`)
- `FlexWeight`: Allows flexible sizing in layouts
- `TextTooltipStyle`: For tooltips with show delay
- `ScrollbarStyle`: For scrolling behavior

### Styles
Styles define appearance and behavior:
- `ButtonStyle`: For buttons (Default, Hovered, Pressed, Disabled states)
- `LabelStyle`: For text labels (FontSize, TextColor, Alignment, etc.)
- `TextTooltipStyle`: For tooltips
- `ScrollbarStyle`: For scrollbars (Background, Handle, Spacing, etc.)
- `CheckBoxStyle`: For checkboxes (Checked/Unchecked states)
- `InputFieldStyle`: For text inputs
- `DropdownBoxStyle`: For dropdowns (Backgrounds, Arrows, Labels, etc.)
- `ColorPickerStyle`: For color selection
- `SliderStyle`: For sliders
- `TabNavigationStyle`: For tabbed interfaces
- And many more (PopupMenuLayerStyle, FileDropdownBoxStyle, etc.)

## Common Components

### Basic Components
- **Group**: Container for other components, supports LayoutMode
- **Label**: Text display
- **Button**: Clickable element
- **TextButton**: Button with text
- **CheckBox**: Toggleable checkbox
- **TextField**: Single-line text input
- **NumberField**: Numeric input
- **DropdownBox**: Selection dropdown
- **Sprite**: Animated image
- **TimerLabel**: Time display
- **AssetImage**: For displaying assets like memory icons

### Specialized Components
- **SceneBlur**: Background blur effect
- **MultilineTextField**: Multi-line text input
- **CompactTextField**: Collapsible text field
- **ColorPicker**: Color selection
- **Slider**: Value slider
- **CompactTextField**: Collapsible text input with expand/collapse
- **BackButton**: Navigation button
- **TextTooltip**: Tooltip component

### Layout Components
- **Container**: Standard container with header and content
- **DecoratedContainer**: Container with decorative elements
- **Panel**: Simple panel background
- **PageOverlay**: Full-screen overlay with background color

## Folder Breakdown

### 
This file defines the bulk of reusable styles and components.

#### Key Constants
- Button dimensions: `@PrimaryButtonHeight = 44`, `@SmallButtonHeight = 32`, etc.
- Colors: `@DisabledColor = #797b7c`
- Paddings: `@ButtonPadding = 24`
- Heights: `@TitleHeight = 38`, `@DropdownBoxHeight = 32`

#### Button Styles
- `@DefaultButtonStyle`: Standard button with primary background
- `@SecondaryButtonStyle`: Alternative button style
- `@CancelButtonStyle`: Destructive action button
- `@SmallDefaultTextButtonStyle`: Compact text button
- `@TertiaryButtonStyle`: Tertiary button style
- `@CancelTextButtonStyle`: Cancel text button
- `@SecondaryTextButtonStyle`: Secondary text button
- `@SmallSecondaryTextButtonStyle`: Small secondary text button
- `@TertiaryTextButtonStyle`: Tertiary text button

#### Label Styles
- `@DefaultLabelStyle`: Basic label (FontSize: 16, TextColor: #96a9be)
- `@TitleStyle`: Bold, uppercase titles (FontSize: 15, TextColor: #b4c8c9, FontName: "Secondary")
- `@SubtitleStyle`: Smaller subtitles (FontSize: 15, RenderUppercase: true, TextColor: #96a9be)
- `@PopupTitleStyle`: Large popup titles (FontSize: 38, LetterSpacing: 2)
- `@HeaderTextButtonLabelStyle`: For header buttons

#### Layout Components
- `@Container`: Standard container with title and content areas, optional close button
- `@DecoratedContainer`: Enhanced container with decorations (top/bottom)
- `@Panel`: Simple panel with patch background
- `@PageOverlay`: Overlay with semi-transparent background (#000000(0.45))
- `@PanelTitle`: Titled panel section with separator
- `@ContentSeparator`: Horizontal separator line
- `@VerticalSeparator`: Vertical separator with texture
- `@PanelSeparatorFancy`: Decorative separator with line and decoration

#### Interactive Components
- `@TextButton`: Pre-styled text button with sounds
- `@Button`: Square button component
- `@CheckBox`: Checkbox with label support
- `@CheckBoxWithLabel`: Combined checkbox and label
- `@TextField` / `@NumberField`: Input fields with backgrounds
- `@DropdownBox`: Configurable dropdown with scrollbar
- `@CloseButton`: Close button for containers
- `@BackButton`: Navigation back button
- `@HeaderSearch`: Search input with icon and clear button
- `@HeaderTextButton`: Header text button
- `@DefaultSpinner`: Animated loading spinner (72 frames)

#### Advanced Components
- `@ActionButtonContainer`: Container for action buttons with separators
- `@TopTabsStyle`: Tab navigation for top tabs
- `@HeaderTabsStyle`: Tab navigation for header tabs
- `@DefaultColorPickerStyle`: Color picker with dropdown
- `@DefaultSliderStyle`: Slider with handle and sounds
- `@DefaultTextTooltipStyle`: Tooltip with background and padding

#### Sound Integration
Sound references from `$Sounds`:
- `@ButtonsLight`: Light button interactions
- `@ButtonsCancel`: Cancel/destructive actions
- `@DropdownBox`: Dropdown interactions
- `@ButtonsLightHover`: Hover sounds
- And more (Tick, Untick, Save, etc.)

### 
Defines sound effects for UI interactions:
- `@ButtonsLight`: Light button sounds
- `@ButtonsCancel`: Cancel/destructive sounds
- `@DropdownBox`: Dropdown interactions
- `@ButtonsLightHover`: Hover sounds
- And more (Tick, Untick, Save, etc.)

### 
Defines action buttons with input bindings:
- Styles for labels, bindings, and icons
- Container with name and binding display
- Supports mouse click icons (left, middle, right)
- Modifier keys and binding labels

### 
Text button component with selection states:
- Basic text button with hover background
- Selected state with bold text

### 
HUD element displaying remaining time:
- Background overlay
- Clock icon
- TimerLabel with 15-minute countdown

### Custom/Pages/ (Examples)

#### 
Shop interface:
- Page overlay with semi-transparent background
- Container with title and scrolling content area
- Back button for navigation
- Element list with vertical scrolling and scrollbar

```ui
$C.@PageOverlay {}

$C.@Container {
  Anchor: (Width: 600, Height: 700);

  #Title {
    Group {
      $C.@Title {
        @Text = %server.customUI.shopPage.title;
      }
    }
  }

  #Content {
    LayoutMode: Left;

    Group #ElementList {
      FlexWeight: 1;
      LayoutMode: TopScrolling;
      ScrollbarStyle: $C.@DefaultScrollbarStyle;
    }
  }
}

$C.@BackButton {}
```

#### 
Memories system interface:
- Decorated container with title
- Two-panel layout: left for category grid, right for memory details
- Category header with title and count
- Memory grid with scrolling and wrapping layout
- Memory details with name, large icon, and time/location
- Back button

```ui
$C.@PageOverlay {
  LayoutMode: Middle;

  $C.@DecoratedContainer {
    Anchor: (Height: 825, Width: 1070);

    #Title { ... }

    #Content {
      LayoutMode: Top;
      Padding: (Full: 9);

      Group {
        LayoutMode: Left;
        FlexWeight: 1;

        // Left Panel - Memory Grid
        Group #LeftPanel {
          LayoutMode: Top;
          FlexWeight: 1;
          Padding: (Left: 20, Right: 5, Vertical: 16);

          // Category Title
          Group #CategoryHeader { ... }

          // Memory Grid
          Group {
            FlexWeight: 1;
            Group {
              LayoutMode: TopScrolling;
              ScrollbarStyle: $C.@DefaultScrollbarStyle;

              Group #IconList {
                LayoutMode: LeftCenterWrap;
              }
            }
          }

          // Back Button
          $C.@SecondaryTextButton #BackButton { ... }
        }

        // Right Panel - Memory Details
        Group #RightPanel {
          LayoutMode: Top;
          FlexWeight: 1;
          Padding: (Right: 20, Left: 5, Vertical: 16);

          Group #Background {
            Background: (TexturePath: "ContainerBackgroundSecondary.png", Border: 23);
            LayoutMode: Top;
            FlexWeight: 1;

            // Memory Name
            Label #MemoryName { ... }

            // Memory Icon (Large)
            AssetImage #MemoryIcon { ... }

            Label #MemoryTimeLocation { ... }
          }
        }
      }
    }
  }
}
```

#### 
Chest marker overlay:
- Invisible by default
- Chest icons (disabled/active states)
- Arrow indicator
- Tooltip support with delay

```ui
@ChestGroup = Group {
  Anchor: (Left: 0, Bottom: 0, Width: 40, Height: 40);
  Visible: false;
  TextTooltipShowDelay: 0.1;
  LayoutMode: Bottom;
  TextTooltipStyle: $Common.@DefaultTextTooltipStyle;
};

Group #ChestContainer {
  Anchor: (Bottom: 18, Left: -2);

  @ChestGroup #ChestDisabled {
    Group {
      Background: "MemoriesProgress/IconChestDisabled.png";
      Anchor: (Width: 22, Height: 22, Bottom: 10);
    }
  }

  @ChestGroup #ChestActive { ... }

  Group #Arrow {
    Background: "MemoriesProgress/MemoriesBarArrow.png";
    Anchor: (Left: 14, Width: 10, Height: 8, Bottom: 0);
    Visible: false;
  }
}
```

#### 
Portal summoning interface (partial read):
- Four-frame border component for custom framing
- Uses texture paths for frame pieces (Top, Right, Bottom, Left)

```ui
@FourFrame = Group {
  @FrameShort = 1;
  @FrameLong = 88;

  Group {
    Anchor: (Left: @FrameShort, Top: 0, Width: @BoxWidth - @FrameShort * 2, Height: @FrameShort);
    Background: (TexturePath: "FrameTop.png");
  }

  // Similar for Right, Bottom, Left
};
```

## Advanced Usage

### Inheritance and Style Extension
Styles can inherit from others using the `...@BaseStyle` syntax:
```ui
@CustomLabelStyle = LabelStyle(
  ...@DefaultLabelStyle,
  TextColor: #ff0000,
  FontSize: 20
);
```

This allows creating variations while maintaining consistency.

### Component Parameterization
Components can be parameterized with variables:
```ui
@CustomButton = TextButton {
  @Text = "Default";
  @Anchor = Anchor();

  Style: @DefaultTextButtonStyle;
  Anchor: (...@Anchor, Height: @DefaultButtonHeight);
  Text: @Text;
};
```

### Layout Hierarchies
Complex layouts use nested Groups with different LayoutModes:
- `Left`: Horizontal stacking
- `Top`: Vertical stacking
- `TopScrolling`: Vertical with scrollbar
- `LeftCenterWrap`: Grid-like wrapping
- `Middle`: Centered content

### Event Handling and Sounds
Components integrate sound effects through style properties:
```ui
Style: (
  ...@DefaultButtonStyle,
  Sounds: (
    ...$Sounds.@ButtonsLight,
    MouseHover: (SoundPath: $Sounds.@ButtonsLightHover, Volume: 6)
  )
);
```

### Tooltips and Interactions
Tooltips are added with `TextTooltipStyle` and `TextTooltipShowDelay`:
```ui
TextTooltipStyle: @DefaultTextTooltipStyle;
TextTooltipShowDelay: 0.1;
```

### Animations and Sprites
Animated elements use `Sprite` with frame definitions:
```ui
Sprite {
  TexturePath: "Spinner.png";
  Frame: (Width: 32, Height: 32, PerRow: 8, Count: 72);
  FramesPerSecond: 30;
}
```

## Common Patterns

### Layout Modes
- `Left`: Horizontal layout
- `Top`: Vertical layout
- `TopScrolling`: Vertical with scrollbar
- `LeftCenterWrap`: Grid-like wrapping
- `Middle`: Center alignment

### Anchoring
Positions use relative anchors like `Top: 20`, `Left: 0`, with optional Width/Height.
Negative values position from opposite edges.

### Styling Inheritance
Styles use `...@BaseStyle` for inheritance, e.g., `(...@DefaultButtonLabelStyle, TextColor: @DisabledColor)`

### Localization
Text uses keys like `%server.customUI.shopPage.title` for internationalization.

### Sound Integration
Components reference sound groups from  for audio feedback.

### ID References
Components use `#Name` for identification, allowing code-side access.

## Advanced Usage Patterns

### Style Inheritance and Overrides
Styles support inheritance using the `...@BaseStyle` syntax, allowing customization while maintaining consistency:
```ui
@CustomButtonStyle = ButtonStyle {
  ...@DefaultButtonStyle;
  Background: @CustomBackground;
  Hovered: {
    Background: @CustomHoveredBackground;
  }
}
```

### Component Parameterization
Components can be parameterized for flexibility:
```ui
@ParameterizedButton = TextButton {
  Text: %param.text;
  Style: %param.style;
  Anchor: %param.anchor;
}
```

### Complex Layout Hierarchies
Deep nesting with Groups for structured layouts:
```ui
Group #MainContainer {
  LayoutMode: Top;
  Group #Header {
    LayoutMode: Left;
    Label #Title { Text: "Title"; }
    Button #Close { ... }
  }
  Group #Content {
    LayoutMode: TopScrolling;
    // Content items
  }
}
```

### Dynamic Content with AssetImage
Use AssetImage for loading dynamic assets:
```ui
AssetImage #MemoryIcon {
  Anchor: (Width: 64, Height: 64);
  AssetId: %memory.assetId;
}
```

### Animated Sprites
Sprites support animation sequences:
```ui
Sprite #AnimatedIcon {
  Anchor: (Width: 32, Height: 32);
  SpriteSheet: "icons.png";
  FrameSize: (32, 32);
  FrameCount: 8;
  FrameRate: 12;
}
```

## Event Handling

### Sound Integration
Components integrate sounds for feedback:
```ui
Button #ActionButton {
  Style: @DefaultButtonStyle;
  Sound: $Sounds.@ButtonsLight;
}
```

### Tooltips
Tooltips provide contextual information:
```ui
Group #InteractiveElement {
  TextTooltipShowDelay: 0.5;
  TextTooltipStyle: $Common.@DefaultTextTooltipStyle;
  TextTooltipText: %tooltip.text;
}
```

### Interactive States
Buttons and inputs handle multiple states:
- Default, Hovered, Pressed, Disabled for buttons
- Focused, Error states for inputs

## Code Examples

### Complete Page Structure (from ShopPage.ui)
```ui
$Common = "../../Common.ui";
$Sounds = "../Sounds.ui";

PageOverlay #ShopPage {
  Style: $Common.@DefaultPageOverlayStyle;
  
  Group #Container {
    Style: $Common.@DecoratedContainer;
    
    Group #Title {
      Style: $Common.@TitleStyle;
      Text: %server.customUI.shopPage.title;
    }
    
    Group #Content {
      LayoutMode: TopScrolling;
      // Shop items
    }
    
    BackButton #Back {
      Style: $Common.@BackButtonStyle;
    }
  }
}
```

### Memories Panel (from MemoriesPanel.ui)
```ui
DecoratedContainer #MemoriesPanel {
  Group #LeftPanel {
    LayoutMode: Top;
    // Category grid
  }
  
  Group #RightPanel {
    LayoutMode: Top;
    // Memory details
  }
}
```

### Portal Interface (from Portals.ui)
```ui
Group #PortalSummon {
  // Panes with dimensions and styles
  Group #Pane1 { Anchor: (Width: 200, Height: 100); }
  Group #Pane2 { Anchor: (Width: 150, Height: 80); }
  
  Pill #TimeLimit {
    Style: @PillStyle;
    Text: "5:00";
  }
}
```

## Best Practices

- Use `@` prefixed names for reusable definitions.
- Leverage  for consistency.
- Include sounds for better UX.
- Use LayoutMode for responsive layouts.
- Reference styles from  rather than redefining.
- Use localization keys for text.
- Structure pages with PageOverlay, Container, and BackButton.
- Use DecoratedContainer for important interfaces.
- Implement tooltips for interactive elements.
- Use AssetImage for dynamic content like icons.

This system provides a flexible, component-based approach to UI definition, with strong emphasis on reusability and consistency through shared styles and components.