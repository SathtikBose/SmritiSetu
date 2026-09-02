import { Stack } from "expo-router";

import "../../global.css"; // Ensure global CSS is imported if nativewind v4 is used

export default function RootLayout() {
  return (
    <Stack screenOptions={{ headerShown: false, animation: 'fade' }}>
      <Stack.Screen name="(tabs)" />
    </Stack>
  );
}
