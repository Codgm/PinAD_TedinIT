import { useState } from 'react';
import styles from '@/app/styles/EventTemplete.module.css';

const AppointmentForm = () => {
  const [appointmentData, setAppointmentData] = useState({
    location: '',
    withWhom: '',
    date: '',
    purpose: '',
    items: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setAppointmentData((prevData) => ({ ...prevData, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // 제출 처리 로직 추가
    console.log(appointmentData);
  };

  return (
    <div className={styles.container}>
      <form onSubmit={handleSubmit}>
      <div className={styles.formGroup}>
          <label htmlFor="location" className="block text-sm font-medium text-gray-700">장소 이름</label>
          <input
            type="text"
            id="location"
            name="location"
            placeholder="장소"
            value={appointmentData.location}
            onChange={handleChange}
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>

        <div className={styles.formGroup}>
        <label htmlFor="withWhom" className="block text-sm font-medium text-gray-700">누구와</label>
          <input
            type="text"
            id="withWhom"
            name="withWhom"
            placeholder="예: 친구, 가족, 동료 등"
            value={appointmentData.withWhom}
            onChange={handleChange}
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>

        <div className={styles.formGroup}>
        <label htmlFor="date" className="block text-sm font-medium text-gray-700">언제</label>
          <input
            type="datetime-local"
            id="date"
            name="date"
            value={appointmentData.date}
            onChange={handleChange}
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>

        <div className={styles.formGroup}>
        <label htmlFor="purpose" className="block text-sm font-medium text-gray-700">목적</label>
          <textarea
            id="purpose"
            name="purpose"
            placeholder="약속의 목적을 적어주세요."
            value={appointmentData.purpose}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>

        <div className={styles.formGroup}>
            <label htmlFor="items" className="block text-sm font-medium text-gray-700">준비물</label>
          <textarea
            id="items"
            name="items"
            placeholder="필요한 준비물이나 참고 사항을 적어주세요."
            value={appointmentData.items}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>
      </form>
    </div>
  );
};

export default AppointmentForm;
