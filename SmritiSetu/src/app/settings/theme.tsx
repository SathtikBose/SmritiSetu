import { View, Text, TouchableOpacity, SafeAreaView, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ArrowLeft, CheckCircle2, Moon, Sun, Monitor } from 'lucide-react-native';

export default function ThemeScreen() {
  const router = useRouter();
  const [selectedTheme, setSelectedTheme] = useState('dark');

  const themes = [
    { id: 'dark', title: 'Dark Mode', icon: Moon, desc: 'Perfect for low-light environments' },
    { id: 'light', title: 'Light Mode', icon: Sun, desc: 'Clean and bright appearance' },
    { id: 'system', title: 'System Default', icon: Monitor, desc: 'Adapts to your device settings' },
  ];

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 px-6 pt-8">
        <View className="flex-row items-center mb-8">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center border border-white/10 mr-4"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          <Text className="text-3xl font-bold text-white">App Theme</Text>
        </View>

        <ScrollView showsVerticalScrollIndicator={false}>
          <Text className="text-slate-400 font-medium mb-6 px-1">Choose how SmritiSetu looks to you.</Text>
          
          <View className="space-y-4">
            {themes.map((theme) => {
              const Icon = theme.icon;
              const isSelected = selectedTheme === theme.id;
              
              return (
                <TouchableOpacity
                  key={theme.id}
                  className={`p-6 rounded-3xl border mb-4 flex-row items-center ${
                    isSelected ? 'bg-indigo-500/20 border-indigo-500/50' : 'bg-white/5 border-white/10'
                  }`}
                  onPress={() => setSelectedTheme(theme.id)}
                >
                  <View className={`p-3 rounded-2xl mr-4 ${isSelected ? 'bg-indigo-500/30' : 'bg-white/10'}`}>
                    <Icon color={isSelected ? '#818cf8' : '#cbd5e1'} size={24} />
                  </View>
                  
                  <View className="flex-1">
                    <Text className={`text-xl font-bold mb-1 ${isSelected ? 'text-indigo-300' : 'text-white'}`}>
                      {theme.title}
                    </Text>
                    <Text className="text-slate-400 text-sm">{theme.desc}</Text>
                  </View>
                  
                  {isSelected && <CheckCircle2 color="#818cf8" size={24} />}
                </TouchableOpacity>
              );
            })}
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}
