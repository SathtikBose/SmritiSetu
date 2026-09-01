import { Text, View, TouchableOpacity, ScrollView, Alert } from "react-native";
import { User, Moon, LogOut, RefreshCw, ChevronRight } from "lucide-react-native";
import { Link, useRouter } from "expo-router";

export default function SettingsScreen() {
  const router = useRouter();

  const handleCheckForUpdates = () => {
    Alert.alert("Update Check", "Your app is up to date!");
  };

  const handleLogout = () => {
    // @ts-ignore
    router.replace("/login");
  };

  return (
    <ScrollView className="flex-1 bg-slate-900 px-4 py-8">
      <Text className="text-3xl font-bold text-indigo-400 mb-8 pt-8 px-2">Settings</Text>
      
      <View className="space-y-6">
        {/* Account Section */}
        <View>
          <Text className="text-slate-400 font-semibold mb-2 px-2 uppercase tracking-wider text-sm">Account</Text>
          <View className="bg-slate-800 rounded-xl overflow-hidden">
            {/* @ts-ignore */}
            <Link href="/profile" asChild>
              <TouchableOpacity className="flex-row items-center justify-between p-4 border-b border-slate-700">
                <View className="flex-row items-center">
                  <User color="#818cf8" size={22} />
                  <Text className="text-slate-200 text-base ml-3">Profile</Text>
                </View>
                <ChevronRight color="#475569" size={20} />
              </TouchableOpacity>
            </Link>
          </View>
        </View>

        {/* Preferences Section */}
        <View>
          <Text className="text-slate-400 font-semibold mb-2 px-2 uppercase tracking-wider text-sm">Preferences</Text>
          <View className="bg-slate-800 rounded-xl overflow-hidden">
            <TouchableOpacity className="flex-row items-center justify-between p-4 border-b border-slate-700">
              <View className="flex-row items-center">
                <Moon color="#818cf8" size={22} />
                <Text className="text-slate-200 text-base ml-3">App Theme</Text>
              </View>
              <View className="flex-row items-center">
                <Text className="text-slate-400 mr-2">System</Text>
                <ChevronRight color="#475569" size={20} />
              </View>
            </TouchableOpacity>
          </View>
        </View>

        {/* About Section */}
        <View>
          <Text className="text-slate-400 font-semibold mb-2 px-2 uppercase tracking-wider text-sm">About</Text>
          <View className="bg-slate-800 rounded-xl overflow-hidden">
            <TouchableOpacity 
              className="flex-row items-center justify-between p-4 border-b border-slate-700"
              onPress={handleCheckForUpdates}
            >
              <View className="flex-row items-center">
                <RefreshCw color="#818cf8" size={22} />
                <Text className="text-slate-200 text-base ml-3">Check for Updates</Text>
              </View>
            </TouchableOpacity>
            
            <View className="flex-row items-center justify-between p-4">
              <Text className="text-slate-400 text-base">App Version</Text>
              <Text className="text-slate-500">1.0.0</Text>
            </View>
          </View>
        </View>

        {/* Actions */}
        <View className="pt-4 pb-12">
          <TouchableOpacity 
            className="flex-row items-center justify-center p-4 bg-slate-800 rounded-xl"
            onPress={handleLogout}
          >
            <LogOut color="#ef4444" size={22} />
            <Text className="text-red-500 font-semibold text-lg ml-2">Logout</Text>
          </TouchableOpacity>
        </View>

      </View>
    </ScrollView>
  );
}
