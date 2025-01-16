package org.lushplugins.followers.utils.menu;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.utils.SignMenuFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
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
        private BiConsumer<String, Player> onCompletion = (output, player) -> {};
        private BiConsumer<String, Player> onCancel = (output, player) -> {};
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

        public Builder onCompletion(BiConsumer<String, Player>  onCompletion) {
            this.onCompletion = onCompletion;
            return this;
        }

        public Builder onCancel(BiConsumer<String, Player> onCancel) {
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
                            onCompletion.accept(output[0], player);
                        } else {
                            onCancel.accept(output[0], player);
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