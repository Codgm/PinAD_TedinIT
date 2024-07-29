"use client"
import { useState } from 'react';
import styles from '../styles/LanguageSwitcher.module.css'; // 스타일 파일도 함께 생성해야 합니다.

export default function LanguageSwitcher() {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedLanguage, setSelectedLanguage] = useState('한국어');

  const languages = ['한국어', '중국어', '영어', '일본어'];

  const toggleMenu = () => setIsOpen(!isOpen);
  const selectLanguage = (language) => {
    setSelectedLanguage(language);
    setIsOpen(false);
  };

  return (
    <div className={styles.languageSwitcher}>
      <button onClick={toggleMenu} className={styles.languageButton}>
        {selectedLanguage} <svg className={styles.arrow} width="24" height="24" fill="none" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {isOpen && (
        <ul className={styles.languageMenu}>
          {languages.map((lang) => (
            <li key={lang} onClick={() => selectLanguage(lang)} className={styles.languageItem}>
              {lang}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
