package org.lushplugins.followers.utils.menu;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.SignMenuFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextInterface {

    private TextInterface() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String prompt = "Enter an input:";
        private String initialInput = "";
        private Function<String, Boolean> textPredicate = (input) -> true;
        private Consumer<String> onCompletion = (input) -> {};
        private Consumer<String> onCancel = (input) -> {};
        private InputType inputType = InputType.ANVIL;

        private Builder() {}

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder initialInput(String initialInput) {
            this.initialInput = initialInput;
            return this;
        }

        /**
         * Set the text predicate for the text interface, this does not guarantee that the result will match this predicate
         * @param textPredicate text predicate
         */
        public Builder textPredicate(Function<String, Boolean> textPredicate) {
            this.textPredicate = textPredicate;
            return this;
        }

        public Builder onCompletion(Consumer<String> onCompletion) {
            this.onCompletion = onCompletion;
            return this;
        }

        public Builder onCancel(Consumer<String> onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public Builder inputType(InputType inputType) {
            this.inputType = inputType;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public void open(Player player) {
            if (Followers.getInstance().hasFloodgate() && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                new FloodgateFormFactory()
                    .form(
                        player,
                        onCompletion,
                        prompt,
                        "Input",
                        initialInput
                    );
            } else if (inputType == InputType.ANVIL) {
                AnvilMenu.builder()
                    .prompt(prompt)
                    .initialInput(initialInput)
                    .textPredicate(textPredicate)
                    .onCompletion(onCompletion)
                    .onCancel(onCancel)
                    .open(player);
            } else {
                new SignMenuFactory()
                    .newMenu(Arrays.asList("", "^^^^^^^^^^^", prompt, ""))
                    .reopenIfFail(true)
                    .response((ignored, output) -> {
                        if (textPredicate.apply(output[0])) {
                            onCompletion.accept(output[0]);
                        } else {
                            onCancel.accept(output[0]);
                        }

                        return true;
                    })
                    .open(player);
            }
        }
    }

    public enum InputType {
        ANVIL,
        SIGN
    }
}