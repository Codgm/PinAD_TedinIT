import { useState } from 'react';
import styles from '@/app/styles/EventTemplete.module.css';

const TravelmemoComponent = () => {
    
  const [travelData, setTravelData] = useState({
    title: '',
    location: '',
    date: '',
    details: {
      event: '',
      emotion: '',
      memorable_moment: '',
      peopleMet: '',
      unexpected_discovery: '',
      self_at_place: '',
      thoughts: ['', '', ''],
      final_message: ''
    }
  });
  const handleThoughtChange = (index, value) => {
    const updatedThoughts = [...reflectionData.thoughts];
    updatedThoughts[index] = value;
    setReflectionData((prevData) => ({ ...prevData, thoughts: updatedThoughts }));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setTravelData((prevData) => ({ ...prevData, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // 제출 처리 로직 추가
    console.log(travelData);
  };

  return (
    <div className={styles.container}>
      <form onSubmit={handleSubmit}>
      <div className='flex items-center w-full space-x-2'>
        <div className={styles.formGroup}>
          <input
            type="text"
            id="title"
            name="title"
            placeholder="장소 이름"
            value={travelData.title}
            onChange={handleChange}
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>
        <div className={styles.formGroup}>
          <input
            type="text"
            id="location"
            name="location"
            placeholder="장소 위치"
            value={travelData.location}
            onChange={handleChange}
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            required
          />
        </div>
      </div>

    <div className={styles.formGroup}>
    {/* <label htmlFor="date" className="block text-sm font-medium text-gray-700">날짜</label> */}
    <input
        type="datetime-local"
        id="date"
        name="date"
        placeholder='날짜'
        value={travelData.date}
        onChange={handleChange}
        required
      />
    </div>

    <div className={styles.formGroup}>
      <label htmlFor="event" className="block text-sm font-medium text-gray-700">순간의 감정</label>
      <input
        id="event"
        name="event"
        value={travelData.event}
        onChange={handleChange}
        placeholder='이 순간 느낌 감정을 한 문장으로..'
        className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
        required
      />
    </div>

    <div className="p-6 max-w-lg mx-auto bg-white rounded-lg shadow-lg">
      <h1 className="text-lg font-bold mb-4">오감으로 담아낸 이곳</h1>
      <form onSubmit={handleSubmit}>
        <div className="mb-4">
          <textarea
            id="sight"
            name="sight"
            placeholder="시각"
            value={travelData.sight}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className="mb-4">
          <textarea
            id="sound"
            name="sound"
            placeholder='청각'
            value={travelData.sound}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className="mb-4">
          <textarea
            id="touch"
            name="touch"
            placeholder='촉각'
            value={travelData.touch}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className="mb-4">
          <textarea
            id="smell"
            name="smell"
            placeholder='후각'
            value={travelData.smell}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className="mb-4">
          <textarea
            id="taste"
            name="taste"
            placeholder='미각'
            value={travelData.taste}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>
      </form>
    </div>

    <div className={styles.formGroup}>
    <label htmlFor="memorable_moment" className="block text-sm font-medium text-gray-700">마음에 세겨진 한컷</label>
          <input
            id="memorable_moment"
            name="memorable_moment"
            placeholder='가장 인상 깊은 장면,순간'
            value={travelData.memorable_moment}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
    </div>
    <div className={styles.formGroup}>
    {/* <label htmlFor="peopleMet" className="block text-sm font-medium text-gray-700">만난 사람들</label> */}
          <input
            id="peopleMet"
            name="peopleMet"
            placeholder='특별히 기억에 남는 사람,대화'
            value={travelData.peopleMet}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
    </div>
    <div className={styles.formGroup}>
    {/* <label htmlFor="unexpected_discovery" className="block text-sm font-medium text-gray-700">예상 밖의 발견</label> */}
          <input
            id="unexpected_discovery"
            name="unexpected_discovery"
            placeholder='예상치 못한 발견,경험'
            value={travelData.unexpected_discovery}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className={styles.formGroup}>
        {/* <label htmlFor="self_at_place" className="block text-sm font-medium text-gray-700">이곳에서의 나</label> */}
          <textarea
            id="self_at_place"
            name="self_at_place"
            placeholder='이곳에서의 나의 감정,생각,변화'
            value={travelData.self_at_place}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className={styles.formGroup}>
        <label className="block text-sm font-medium text-gray-700">떠오르는 생각들</label>
          {travelData.details.thoughts.map((thought, index) => (
            <input
              key={index}
              value={thought}
              onChange={(e) => handleThoughtChange(index, e.target.value)}
              rows="1"
              placeholder={`생각 ${index + 1}`}
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm mb-2"
            />
          ))}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="final_message" className="block text-sm font-medium text-gray-700">남기고 싶은 한마디</label>
          <input
            id="final_message"
            name="final_message"
            value={travelData.final_message}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

  </form>
</div>);
}

export default TravelmemoComponent