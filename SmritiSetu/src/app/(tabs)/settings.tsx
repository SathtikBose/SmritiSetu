import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { Href, useRouter } from 'expo-router';

export default function SettingsScreen() {
  const router = useRouter();

  const settingsOptions: { title: string, route: any }[] = [
    { title: 'Profile', route: '/settings/profile' },
    { title: 'Edit Profile', route: '/settings/edit-profile' },
    { title: 'Change Password', route: '/settings/change-password' },
    { title: 'App Theme', route: '/settings/theme' },
  ];

  return (
    <ScrollView className="flex-1 bg-slate-900 pt-12 px-6">
      <Text className="text-3xl font-bold text-white mb-8">Settings</Text>
      
      <View className="bg-slate-800 rounded-2xl overflow-hidden mb-6">
        {settingsOptions.map((option, index) => (
          <TouchableOpacity
            key={index}
            className={`p-4 ${index !== settingsOptions.length - 1 ? 'border-b border-slate-700' : ''}`}
            onPress={() => router.push(option.route)}
          >
            <Text className="text-white text-lg font-medium">{option.title}</Text>
          </TouchableOpacity>
        ))}
      </View>
      
      <TouchableOpacity 
        className="bg-indigo-500 p-4 rounded-xl items-center mb-6"
        onPress={() => router.replace('/login')}
      >
        <Text className="text-white font-semibold text-lg">Log Out</Text>
      </TouchableOpacity>
      
      <Text className="text-slate-500 text-center">App Version 1.0.0</Text>
    </ScrollView>
  );
}
