import { View, Text, TouchableOpacity, ScrollView, SafeAreaView } from 'react-native';
import { useRouter } from 'expo-router';
import { User, Edit3, Key, Palette, LogOut, ChevronRight } from 'lucide-react-native';

export default function SettingsScreen() {
  const router = useRouter();

  const settingsOptions: { title: string, route: any, icon: any }[] = [
    { title: 'Profile', route: '/settings/profile', icon: User },
    { title: 'Edit Profile', route: '/settings/edit-profile', icon: Edit3 },
    { title: 'Change Password', route: '/settings/change-password', icon: Key },
    { title: 'App Theme', route: '/settings/theme', icon: Palette },
  ];

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <ScrollView className="flex-1 px-6 pt-8">
        <Text className="text-3xl font-bold text-white mb-8">Settings</Text>
        
        <View className="bg-white/10 border border-white/10 rounded-3xl overflow-hidden mb-6 shadow-xl">
          {settingsOptions.map((option, index) => {
            const Icon = option.icon;
            return (
              <TouchableOpacity
                key={index}
                className={`p-5 flex-row items-center justify-between ${index !== settingsOptions.length - 1 ? 'border-b border-white/5' : ''}`}
                onPress={() => router.push(option.route)}
              >
                <View className="flex-row items-center">
                  <View className="bg-indigo-500/20 p-2 rounded-xl mr-4 border border-indigo-400/20">
                    <Icon color="#818cf8" size={20} />
                  </View>
                  <Text className="text-white text-lg font-medium">{option.title}</Text>
                </View>
                <ChevronRight color="#475569" size={20} />
              </TouchableOpacity>
            );
          })}
        </View>
        
        <TouchableOpacity 
          className="bg-red-500/20 border border-red-500/30 p-5 rounded-3xl flex-row items-center justify-center mb-8"
          onPress={() => router.replace('/login')}
        >
          <LogOut color="#ef4444" size={20} className="mr-3" />
          <Text className="text-red-400 font-semibold text-lg">Log Out</Text>
        </TouchableOpacity>
        
        <Text className="text-slate-600 text-center mb-8 font-medium">App Version 1.0.0</Text>
      </ScrollView>
    </SafeAreaView>
  );
}
