package nbtech2.fix.client;

import nbtech2.fix.config.ConfigEntries;
import nbtech2.fix.config.ConfigEntries.Entry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NBConfigScreen extends Screen {
    private static final int MAX_CONTENT_WIDTH = 380;
    private static final int PREFERRED_CONTROL_WIDTH = 160;
    private static final int COLUMN_GAP = 10;
    private static final int ROW_HEIGHT = 24;

    private final Screen parent;
    private final int requestedPage;
    private final List<Row> visibleRows = new ArrayList<>();
    private final List<IntField> intFields = new ArrayList<>();

    private int page;
    private int pageCount;
    private int pageSize;
    private Component validationError;

    public NBConfigScreen(Screen parent) {
        this(parent, 0);
    }

    private NBConfigScreen(Screen parent, int page) {
        super(Component.translatable("nbtech2fix.configuration.title"));
        this.parent = parent;
        this.requestedPage = page;
    }

    @Override
    protected void init() {
        visibleRows.clear();
        intFields.clear();
        validationError = null;

        pageSize = Math.max(1, (height - 116) / ROW_HEIGHT);
        pageCount = Math.max(1, (ConfigEntries.ALL.size() + pageSize - 1) / pageSize);
        page = Math.max(0, Math.min(requestedPage, pageCount - 1));

        int contentWidth = Math.min(MAX_CONTENT_WIDTH, width - 40);
        int controlWidth = Math.min(
                PREFERRED_CONTROL_WIDTH,
                Math.max(100, (contentWidth - COLUMN_GAP) / 2)
        );
        int labelWidth = contentWidth - COLUMN_GAP - controlWidth;
        int left = (width - contentWidth) / 2;
        int controlX = left + labelWidth + COLUMN_GAP;
        int firstIndex = page * pageSize;
        int lastIndex = Math.min(firstIndex + pageSize, ConfigEntries.ALL.size());

        for (int index = firstIndex; index < lastIndex; index++) {
            Entry entry = ConfigEntries.ALL.get(index);
            int y = 42 + (index - firstIndex) * ROW_HEIGHT;
            addEntry(entry, left, controlX, y, contentWidth, labelWidth, controlWidth);
        }
        refreshActiveStates();

        int navigationY = height - 52;
        if (page > 0) {
            addRenderableWidget(Button.builder(
                            Component.translatable("nbtech2fix.configuration.previous"),
                            button -> openPage(page - 1)
                    )
                    .bounds(width / 2 - 154, navigationY, 100, 20)
                    .build());
        }
        if (page + 1 < pageCount) {
            addRenderableWidget(Button.builder(
                            Component.translatable("nbtech2fix.configuration.next"),
                            button -> openPage(page + 1)
                    )
                    .bounds(width / 2 + 54, navigationY, 100, 20)
                    .build());
        }

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> closeToParent()
                )
                .bounds(width / 2 - 50, height - 28, 100, 20)
                .build());
    }

    private void addEntry(
            Entry entry,
            int x,
            int controlX,
            int y,
            int width,
            int labelWidth,
            int controlWidth
    ) {
        ForgeConfigSpec.ConfigValue<?> value = entry.value();
        AbstractWidget control;
        if (value instanceof ForgeConfigSpec.BooleanValue booleanValue) {
            control = Button.builder(
                            booleanMessage(booleanValue.get()),
                            pressed -> {
                                booleanValue.set(!booleanValue.get());
                                booleanValue.save();
                                pressed.setMessage(booleanMessage(booleanValue.get()));
                                refreshActiveStates();
                            }
                    )
                    .bounds(controlX, y, controlWidth, 20)
                    .build();
        } else if (value instanceof ForgeConfigSpec.EnumValue<?> enumValue) {
            control = Button.builder(
                            enumMessage(enumValue.get()),
                            pressed -> {
                                cycleEnum(enumValue);
                                enumValue.save();
                                pressed.setMessage(enumMessage(enumValue.get()));
                                refreshActiveStates();
                            }
                    )
                    .bounds(controlX, y, controlWidth, 20)
                    .build();
        } else if (value instanceof ForgeConfigSpec.IntValue intValue) {
            EditBox field = new EditBox(
                    font,
                    controlX,
                    y,
                    controlWidth,
                    20,
                    Component.translatable(entry.translationKey())
            );
            field.setMaxLength(10);
            field.setFilter(NBConfigScreen::isUnsignedIntegerText);
            field.setValue(Integer.toString(intValue.get()));
            intFields.add(new IntField(entry, intValue, field));
            control = field;
        } else {
            return;
        }

        addRenderableWidget(control);
        visibleRows.add(new Row(entry, control, x, y, width, labelWidth));
    }

    private Component booleanMessage(boolean enabled) {
        return Component.translatable(enabled ? "options.on" : "options.off");
    }

    private Component enumMessage(Enum<?> value) {
        return Component.translatable(
                "nbtech2fix.configuration.value." + value.name().toLowerCase(Locale.ROOT)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void cycleEnum(ForgeConfigSpec.EnumValue enumValue) {
        Enum<?> current = (Enum<?>) enumValue.get();
        Object[] values = current.getDeclaringClass().getEnumConstants();
        enumValue.set(values[(current.ordinal() + 1) % values.length]);
    }

    private static boolean isUnsignedIntegerText(String text) {
        if (text.isEmpty()) {
            return true;
        }
        for (int index = 0; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private void refreshActiveStates() {
        for (Row row : visibleRows) {
            boolean active = row.entry().isActive();
            row.control().active = active;
            if (row.control() instanceof EditBox field) {
                field.setTextColor(active ? 0xE0E0E0 : 0x707070);
            }
        }
    }

    private boolean commitIntFields() {
        for (IntField intField : intFields) {
            if (!intField.entry().isActive()) {
                continue;
            }
            try {
                long parsed = Long.parseLong(intField.field().getValue());
                if (parsed < intField.entry().minimum() || parsed > intField.entry().maximum()) {
                    throw new NumberFormatException("Out of range");
                }
                intField.value().set((int) parsed);
                intField.value().save();
                intField.field().setTextColor(0xE0E0E0);
            } catch (NumberFormatException exception) {
                validationError = Component.translatable(
                        "nbtech2fix.configuration.invalid_integer",
                        Component.translatable(intField.entry().translationKey()),
                        intField.entry().minimum(),
                        intField.entry().maximum()
                );
                intField.field().setTextColor(0xFF5555);
                return false;
            }
        }
        validationError = null;
        return true;
    }

    private void openPage(int newPage) {
        if (commitIntFields() && minecraft != null) {
            minecraft.setScreen(new NBConfigScreen(parent, newPage));
        }
    }

    private void closeToParent() {
        if (commitIntFields() && minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void onClose() {
        closeToParent();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (!visibleRows.isEmpty()) {
            int panelLeft = visibleRows.get(0).x() - 12;
            int panelRight = visibleRows.get(0).x() + visibleRows.get(0).width() + 12;
            int panelBottom = visibleRows.get(visibleRows.size() - 1).y() + 24;
            graphics.fill(panelLeft, 34, panelRight, panelBottom, 0xA0000000);
        }
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        if (pageCount > 1) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("nbtech2fix.configuration.page", page + 1, pageCount),
                    width / 2,
                    height - 46,
                    0xA0A0A0
            );
        }

        for (Row row : visibleRows) {
            List<FormattedCharSequence> labelLines = font.split(
                    Component.translatable(row.entry().translationKey()),
                    row.labelWidth() - 4
            );
            FormattedCharSequence label = labelLines.get(0);
            graphics.drawString(
                    font,
                    label,
                    row.x(),
                    row.y() + 6,
                    row.entry().isActive() ? 0xFFFFFF : 0x707070,
                    false
            );
        }

        if (validationError != null) {
            graphics.drawCenteredString(font, validationError, width / 2, height - 68, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        for (Row row : visibleRows) {
            if (mouseX >= row.x()
                    && mouseX < row.x() + row.width()
                    && mouseY >= row.y()
                    && mouseY < row.y() + 20) {
                graphics.renderTooltip(
                        font,
                        Component.translatable(row.entry().tooltipKey()),
                        mouseX,
                        mouseY
                );
                break;
            }
        }
    }

    private record Row(
            Entry entry,
            AbstractWidget control,
            int x,
            int y,
            int width,
            int labelWidth
    ) {
    }

    private record IntField(Entry entry, ForgeConfigSpec.IntValue value, EditBox field) {
    }
}
