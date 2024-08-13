"use client"
import React, { useState } from 'react';
import Home2 from "../components/home2";
import Layout from "@/app/components/layout/layout";

export default function Home() {
  const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);

  const openWriteModal = () => setIsWriteModalOpen(true);
  const closeWriteModal = () => setIsWriteModalOpen(false);
  return (
    <Layout onWriteModal={openWriteModal}>
        <Home2 isWriteModalOpen={isWriteModalOpen} closeWriteModal={closeWriteModal}/>
    </Layout>
  );
}
