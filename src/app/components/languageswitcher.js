"use client";
import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import styles from '../styles/Languageswitcher.module.css';

export default function LanguageSwitcher() {
  const { t, i18n } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  const languages = [
    { code: 'ko', name: '한국어' },
    { code: 'zh', name: '中文' },
    { code: 'en', name: 'English' },
    { code: 'ja', name: '日本語' }
  ];

  const toggleMenu = () => setIsOpen(!isOpen);

  const handleLocaleChange = async (locale) => {
    try {
      // localStorage에 선택한 언어 저장
      localStorage.setItem('selectedLanguage', locale);

      // 언어 변경
      // await i18n.changeLanguage(locale);

      // 페이지를 새로고침하거나 현재 페이지로 라우팅
      router.refresh(); // 페이지를 새로고침하여 변경 사항을 반영

      setIsOpen(false);
    } catch (error) {
      console.error('Error changing language:', error);
    }
  };

  useEffect(() => {
    const loadLanguageFromLocalStorage = async () => {
      // localStorage에서 언어 코드 읽기
      const savedLanguage = localStorage.getItem('selectedLanguage');

      if (savedLanguage && languages.some(l => l.code === savedLanguage)) {
        try {
          await i18n.changeLanguage(savedLanguage);
        } catch (error) {
          console.error('Error initializing language:', error);
        }
      }
    };

    loadLanguageFromLocalStorage();
  }, [i18n]);

  return (
    <div className={styles.languageSwitcher}>
      <button onClick={toggleMenu} className={styles.languageButton}>
        {languages.find(lang => lang.code === i18n.language)?.name || '언어 선택'} 
        <svg className={styles.arrow} width="24" height="24" fill="none" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {isOpen && (
        <ul className={styles.languageMenu}>
          {languages.map((lang) => (
            <li key={lang.code} onClick={() => handleLocaleChange(lang.code)} className={styles.languageItem}>
              {lang.name}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
