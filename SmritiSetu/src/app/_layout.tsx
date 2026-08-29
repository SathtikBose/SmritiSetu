import { Stack } from "expo-router";

import "../../global.css"; // Ensure global CSS is imported if nativewind v4 is used

export default function RootLayout() {
  return (
    <Stack>
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
    </Stack>
  );
}
